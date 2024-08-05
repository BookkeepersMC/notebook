/*
 * Copyright (c) 2023, 2024 BookkeepersMC under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.bookkeepersmc.notebook.impl.resource.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.metadata.ModMetadata;
import com.bookkeepersmc.notebook.api.resource.ModResourcePack;
import com.bookkeepersmc.notebook.api.resource.ResourcePackActivationType;

public class ModNioResourcePack implements PackResources, ModResourcePack {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModNioResourcePack.class);
	private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_.]+");
	private static final FileSystem DEFAULT_FS = FileSystems.getDefault();

	private final String id;
	private final ModContainer mod;
	private final List<Path> basePaths;
	private final PackType type;
	private final ResourcePackActivationType activationType;
	private final Map<PackType, Set<String>> namespaces;
	private final PackLocationInfo metadata;
	/**
	 * Whether the pack is bundled and loaded by default, as opposed to registered built-in packs.
	 * @see ModResourcePackUtil#appendModResourcePacks(List, PackType, String)
	 */
	private final boolean modBundled;

	public static ModNioResourcePack create(String id, ModContainer mod, String subPath, PackType type, ResourcePackActivationType activationType, boolean modBundled) {
		List<Path> rootPaths = mod.getRootPaths();
		List<Path> paths;

		if (subPath == null) {
			paths = rootPaths;
		} else {
			paths = new ArrayList<>(rootPaths.size());

			for (Path path : rootPaths) {
				path = path.toAbsolutePath().normalize();
				Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).normalize();

				if (!childPath.startsWith(path) || !exists(childPath)) {
					continue;
				}

				paths.add(childPath);
			}
		}

		if (paths.isEmpty()) return null;

		String packId = subPath != null && modBundled ? id + "_" + subPath : id;
		Component displayName = subPath == null
				? Component.translatable("pack.name.notebookMod", mod.getMetadata().getName())
				: Component.translatable("pack.name.notebookMod.subPack", mod.getMetadata().getName(), Component.translatable("resourcePack." + subPath + ".name"));
		PackLocationInfo metadata = new PackLocationInfo(
				packId,
				displayName,
				ModResourcePackCreator.RESOURCE_PACK_SOURCE,
				Optional.of(new KnownPack(ModResourcePackCreator.NOTEBOOK, packId, mod.getMetadata().getVersion().getFriendlyString()))
		);
		ModNioResourcePack ret = new ModNioResourcePack(packId, mod, paths, type, activationType, modBundled, metadata);

		return ret.getNamespaces(type).isEmpty() ? null : ret;
	}

	private ModNioResourcePack(String id, ModContainer mod, List<Path> paths, PackType type, ResourcePackActivationType activationType, boolean modBundled, PackLocationInfo metadata) {
		this.id = id;
		this.mod = mod;
		this.basePaths = paths;
		this.type = type;
		this.activationType = activationType;
		this.modBundled = modBundled;
		this.namespaces = readNamespaces(paths, mod.getMetadata().getId());
		this.metadata = metadata;
	}

	@Override
	public ModNioResourcePack createOverlay(String overlay) {
		// See DirectoryResourcePack.
		return new ModNioResourcePack(id, mod, basePaths.stream().map(
				path -> path.resolve(overlay)
		).toList(), type, activationType, modBundled, metadata);
	}

	static Map<PackType, Set<String>> readNamespaces(List<Path> paths, String modId) {
		Map<PackType, Set<String>> ret = new EnumMap<>(PackType.class);

		for (PackType type : PackType.values()) {
			Set<String> namespaces = null;

			for (Path path : paths) {
				Path dir = path.resolve(type.getDirectory());
				if (!Files.isDirectory(dir)) continue;

				String separator = path.getFileSystem().getSeparator();

				try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
					for (Path p : ds) {
						if (!Files.isDirectory(p)) continue;

						String s = p.getFileName().toString();
						// s may contain trailing slashes, remove them
						s = s.replace(separator, "");

						if (!RESOURCE_PACK_PATH.matcher(s).matches()) {
							LOGGER.warn("Notebook NioResourcePack: ignored invalid namespace: {} in mod ID {}", s, modId);
							continue;
						}

						if (namespaces == null) namespaces = new HashSet<>();

						namespaces.add(s);
					}
				} catch (IOException e) {
					LOGGER.warn("getNamespaces in mod " + modId + " failed!", e);
				}
			}

			ret.put(type, namespaces != null ? namespaces : Collections.emptySet());
		}

		return ret;
	}

	private Path getPath(String filename) {
		if (hasAbsentNs(filename)) return null;

		for (Path basePath : basePaths) {
			Path childPath = basePath.resolve(filename.replace("/", basePath.getFileSystem().getSeparator())).toAbsolutePath().normalize();

			if (childPath.startsWith(basePath) && exists(childPath)) {
				return childPath;
			}
		}

		return null;
	}

	private static final String resPrefix = PackType.CLIENT_RESOURCES.getDirectory() + "/";
	private static final String dataPrefix = PackType.SERVER_DATA.getDirectory() + "/";

	private boolean hasAbsentNs(String filename) {
		int prefixLen;
		PackType type;

		if (filename.startsWith(resPrefix)) {
			prefixLen = resPrefix.length();
			type = PackType.CLIENT_RESOURCES;
		} else if (filename.startsWith(dataPrefix)) {
			prefixLen = dataPrefix.length();
			type = PackType.SERVER_DATA;
		} else {
			return false;
		}

		int nsEnd = filename.indexOf('/', prefixLen);
		if (nsEnd < 0) return false;

		return !namespaces.get(type).contains(filename.substring(prefixLen, nsEnd));
	}

	private IoSupplier<InputStream> openFile(String filename) {
		Path path = getPath(filename);

		if (path != null && Files.isRegularFile(path)) {
			return () -> Files.newInputStream(path);
		}

		if (ModResourcePackUtil.containsDefault(filename, this.modBundled)) {
			return () -> ModResourcePackUtil.openDefault(this.mod, this.type, filename);
		}

		return null;
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... pathSegments) {
		FileUtil.validatePath(pathSegments);

		return this.openFile(String.join("/", pathSegments));
	}

	@Override
	@Nullable
	public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
		final Path path = getPath(getFilename(type, id));
		return path == null ? null : IoSupplier.create(path);
	}

	@Override
	public void listResources(PackType type, String namespace, String path, ResourceOutput visitor) {
		if (!namespaces.getOrDefault(type, Collections.emptySet()).contains(namespace)) {
			return;
		}

		for (Path basePath : basePaths) {
			String separator = basePath.getFileSystem().getSeparator();
			Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
			Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
			if (!exists(searchPath)) continue;

			try {
				Files.walkFileTree(searchPath, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						String filename = nsPath.relativize(file).toString().replace(separator, "/");
						ResourceLocation identifier = ResourceLocation.tryBuild(namespace, filename);

						if (identifier == null) {
							LOGGER.error("Invalid path in mod resource-pack {}: {}:{}, ignoring", id, namespace, filename);
						} else {
							visitor.accept(identifier, IoSupplier.create(file));
						}

						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				LOGGER.warn("findResources at " + path + " in namespace " + namespace + ", mod " + mod.getMetadata().getId() + " failed!", e);
			}
		}
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return namespaces.getOrDefault(type, Collections.emptySet());
	}

	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) throws IOException {
		try (InputStream is = Objects.requireNonNull(openFile("pack.mcmeta")).get()) {
			return AbstractPackResources.getMetadataFromStream(metaReader, is);
		}
	}

	@Override
	public PackLocationInfo location() {
		return metadata;
	}

	@Override
	public void close() {
	}

	@Override
	public ModMetadata getNotebookModMetadata() {
		return mod.getMetadata();
	}

	public ResourcePackActivationType getActivationType() {
		return this.activationType;
	}

	@Override
	public String packId() {
		return id;
	}

	private static boolean exists(Path path) {
		// NIO Files.exists is notoriously slow when checking the file system
		return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
	}

	private static String getFilename(PackType type, ResourceLocation id) {
		return String.format(Locale.ROOT, "%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath());
	}
}

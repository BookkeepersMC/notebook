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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.VanillaDataPackProvider;
import net.minecraft.resource.pack.metadata.PackResourceMetadataSection;
import net.minecraft.server.world.FeatureAndDataSettings;
import net.minecraft.text.Text;
import net.minecraft.util.path.SymlinkValidator;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.loader.api.metadata.ModMetadata;
import com.bookkeepersmc.notebook.api.resource.ModResourcePack;
import com.bookkeepersmc.notebook.api.resource.ResourcePackActivationType;

/**
 * Internal utilities for managing resource packs.
 */
public final class ModResourcePackUtil {
	public static final Gson GSON = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(ModResourcePackUtil.class);

	private ModResourcePackUtil() {
	}

	/**
	 * Appends mod resource packs to the given list.
	 *
	 * @param packs   the resource pack list to append
	 * @param type    the type of resource
	 * @param subPath the resource pack sub path directory in mods, may be {@code null}
	 */
	public static void appendModResourcePacks(List<ModResourcePack> packs, ResourceType type, @Nullable String subPath) {
		for (ModContainer container : NotebookLoader.getInstance().getAllMods()) {
			if (container.getMetadata().getType().equals("builtin")) {
				continue;
			}

			ModResourcePack pack = ModNioResourcePack.create(container.getMetadata().getId(), container, subPath, type, ResourcePackActivationType.ALWAYS_ENABLED, true);

			if (pack != null) {
				packs.add(pack);
			}
		}
	}

	public static void refreshAutoEnabledPacks(List<PackProfile> enabledProfiles, Map<String, PackProfile> allProfiles) {
		LOGGER.debug("[Notebook] Starting internal pack sorting with: {}", enabledProfiles.stream().map(PackProfile::getName).toList());
		enabledProfiles.removeIf(profile -> ((NotebookResourcePackProfile) profile).notebook_isHidden());
		LOGGER.debug("[Notebook] Removed all internal packs, result: {}", enabledProfiles.stream().map(PackProfile::getName).toList());
		ListIterator<PackProfile> it = enabledProfiles.listIterator();
		Set<String> seen = new LinkedHashSet<>();

		while (it.hasNext()) {
			PackProfile profile = it.next();
			seen.add(profile.getName());

			for (PackProfile p : allProfiles.values()) {
				NotebookResourcePackProfile fp = (NotebookResourcePackProfile) p;

				if (fp.notebook_isHidden() && fp.notebook_parentsEnabled(seen) && seen.add(p.getName())) {
					it.add(p);
					LOGGER.debug("[Notebook] cur @ {}, auto-enabled {}, currently enabled: {}", profile.getName(), p.getName(), seen);
				}
			}
		}

		LOGGER.debug("[Notebook] Final sorting result: {}", enabledProfiles.stream().map(PackProfile::getName).toList());
	}

	public static boolean containsDefault(String filename, boolean modBundled) {
		return "pack.mcmeta".equals(filename) || (modBundled && "pack.png".equals(filename));
	}

	public static InputStream getDefaultIcon() throws IOException {
		Optional<Path> loaderIconPath = NotebookLoader.getInstance().getModContainer("notebook-resource-loader-v0")
				.flatMap(resourceLoaderContainer -> resourceLoaderContainer.getMetadata().getIconPath(512).flatMap(resourceLoaderContainer::findPath));

		if (loaderIconPath.isPresent()) {
			return Files.newInputStream(loaderIconPath.get());
		}

		// Should never happen in practice
		return null;
	}

	public static InputStream openDefault(ModContainer container, ResourceType type, String filename) throws IOException {
		switch (filename) {
		case "pack.mcmeta":
			String description = Objects.requireNonNullElse(container.getMetadata().getId(), "");
			String metadata = serializeMetadata(SharedConstants.getGameVersion().getResourceVersion(type), description);
			return IOUtils.toInputStream(metadata, Charsets.UTF_8);
		case "pack.png":
			Optional<Path> path = container.getMetadata().getIconPath(512).flatMap(container::findPath);

			if (path.isPresent()) {
				return Files.newInputStream(path.get());
			} else {
				return getDefaultIcon();
			}
		default:
			return null;
		}
	}

	public static PackResourceMetadataSection getMetadataPack(int packVersion, Text description) {
		return new PackResourceMetadataSection(description, packVersion, Optional.empty());
	}

	public static JsonObject getMetadataPackJson(int packVersion, Text description) {
		return PackResourceMetadataSection.TYPE.toJson(getMetadataPack(packVersion, description));
	}

	public static String serializeMetadata(int packVersion, String description) {
		// This seems to be still manually deserialized
		JsonObject pack = getMetadataPackJson(packVersion, Text.literal(description));
		JsonObject metadata = new JsonObject();
		metadata.add("pack", pack);
		return GSON.toJson(metadata);
	}

	public static Text getName(ModMetadata info) {
		if (info.getId() != null) {
			return Text.literal(info.getId());
		} else {
			return Text.translatable("pack.name.notebookMod", info.getId());
		}
	}

	/**
	 * Creates the default data pack settings that replaces
	 * {@code DataPackSettings.SAFE_MODE} used in vanilla.
	 * @return the default data pack settings
	 */
	public static FeatureAndDataSettings createDefaultDataConfiguration() {
		ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(ResourceType.SERVER_DATA);
		List<PackProfile> moddedResourcePacks = new ArrayList<>();
		modResourcePackCreator.loadPacks(moddedResourcePacks::add);

		List<String> enabled = new ArrayList<>(DataPackSettings.SAFE_MODE.getEnabled());
		List<String> disabled = new ArrayList<>(DataPackSettings.SAFE_MODE.getDisabled());

		// This ensures that any built-in registered data packs by mods which needs to be enabled by default are
		// as the data pack screen automatically put any data pack as disabled except the Default data pack.
		for (PackProfile profile : moddedResourcePacks) {
			if (profile.getSource() == ModResourcePackCreator.RESOURCE_PACK_SOURCE) {
				enabled.add(profile.getName());
				continue;
			}

			try (ResourcePack pack = profile.createPack()) {
				if (pack instanceof ModNioResourcePack && ((ModNioResourcePack) pack).getActivationType().isEnabledByDefault()) {
					enabled.add(profile.getName());
				} else {
					disabled.add(profile.getName());
				}
			}
		}

		return new FeatureAndDataSettings(
				new DataPackSettings(enabled, disabled),
				FeatureFlags.DEFAULT_SET
		);
	}

	/**
	 * Vanilla enables all available datapacks automatically in TestServer#create, but it does so in alphabetical order,
	 * which means the Vanilla pack has higher precedence than modded, breaking our tests.
	 * To fix this, we move all modded pack profiles to the end of the list.
	 */
	public static DataPackSettings createTestServerSettings(List<String> enabled, List<String> disabled) {
		// Collect modded profiles
		Set<String> moddedProfiles = new HashSet<>();
		ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(ResourceType.SERVER_DATA);
		modResourcePackCreator.loadPacks(profile -> moddedProfiles.add(profile.getName()));

		// Remove them from the enabled list
		List<String> moveToTheEnd = new ArrayList<>();

		for (Iterator<String> it = enabled.iterator(); it.hasNext();) {
			String profile = it.next();

			if (moddedProfiles.contains(profile)) {
				moveToTheEnd.add(profile);
				it.remove();
			}
		}

		// Add back at the end
		enabled.addAll(moveToTheEnd);

		return new DataPackSettings(enabled, disabled);
	}

	/**
	 * Creates the ResourcePackManager used by the ClientDataPackManager and replaces
	 * {@code VanillaDataPackProvider.createClientManager} used by vanilla.
	 */
	public static PackManager createClientManager() {
		return new PackManager(new VanillaDataPackProvider(new SymlinkValidator((path) -> true)), new ModResourcePackCreator(ResourceType.SERVER_DATA, true));
	}
}

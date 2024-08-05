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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeManager;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.notebook.api.resource.IdentifiableResourceReloadListener;
import com.bookkeepersmc.notebook.api.resource.ResourceManagerHelper;
import com.bookkeepersmc.notebook.api.resource.ResourcePackActivationType;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
	private static final Map<PackType, ResourceManagerHelperImpl> registryMap = new HashMap<>();
	private static final Set<Tuple<Component, ModNioResourcePack>> builtinResourcePacks = new HashSet<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerHelperImpl.class);

	private final Set<ResourceLocation> addedListenerIds = new HashSet<>();
	private final Set<ListenerFactory> listenerFactories = new LinkedHashSet<>();
	private final Set<IdentifiableResourceReloadListener> addedListeners = new LinkedHashSet<>();
	private final PackType type;

	private ResourceManagerHelperImpl(PackType type) {
		this.type = type;
	}

	public static ResourceManagerHelperImpl get(PackType type) {
		return registryMap.computeIfAbsent(type, ResourceManagerHelperImpl::new);
	}

	/**
	 * Registers a built-in resource pack. Internal implementation.
	 *
	 * @param id             the identifier of the resource pack
	 * @param subPath        the sub path in the mod resources
	 * @param container      the mod container
	 * @param displayName    the display name of the resource pack
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, Component, ResourcePackActivationType)
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, ResourcePackActivationType)
	 */
	public static boolean registerBuiltinResourcePack(ResourceLocation id, String subPath, ModContainer container, Component displayName, ResourcePackActivationType activationType) {
		// Assuming the mod has multiple paths, we simply "hope" that the  file separator is *not* different across them
		List<Path> paths = container.getRootPaths();
		String separator = paths.getFirst().getFileSystem().getSeparator();
		subPath = subPath.replace("/", separator);
		ModNioResourcePack resourcePack = ModNioResourcePack.create(id.toString(), container, subPath, PackType.CLIENT_RESOURCES, activationType, false);
		ModNioResourcePack dataPack = ModNioResourcePack.create(id.toString(), container, subPath, PackType.SERVER_DATA, activationType, false);
		if (resourcePack == null && dataPack == null) return false;

		if (resourcePack != null) {
			builtinResourcePacks.add(new Tuple<>(displayName, resourcePack));
		}

		if (dataPack != null) {
			builtinResourcePacks.add(new Tuple<>(displayName, dataPack));
		}

		return true;
	}

	/**
	 * Registers a built-in resource pack. Internal implementation.
	 *
	 * @param id             the identifier of the resource pack
	 * @param subPath        the sub path in the mod resources
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, ResourcePackActivationType)
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(ResourceLocation, ModContainer, Component, ResourcePackActivationType)
	 */
	public static boolean registerBuiltinResourcePack(ResourceLocation id, String subPath, ModContainer container, ResourcePackActivationType activationType) {
		return registerBuiltinResourcePack(id, subPath, container, Component.literal(id.getNamespace() + "/" + id.getPath()), activationType);
	}

	public static void registerBuiltinResourcePacks(PackType resourceType, Consumer<Pack> consumer) {
		// Loop through each registered built-in resource packs and add them if valid.
		for (Tuple<Component, ModNioResourcePack> entry : builtinResourcePacks) {
			ModNioResourcePack pack = entry.getB();

			// Add the built-in pack only if namespaces for the specified resource type are present.
			if (!pack.getNamespaces(resourceType).isEmpty()) {
				// Make the resource pack profile for built-in pack, should never be always enabled.
				PackLocationInfo info = new PackLocationInfo(
						entry.getB().packId(),
						entry.getA(),
						new BuiltinModResourcePackSource(pack.getNotebookModMetadata().getName()),
						entry.getB().knownPackInfo()
				);
				PackSelectionConfig info2 = new PackSelectionConfig(
						pack.getActivationType() == ResourcePackActivationType.ALWAYS_ENABLED,
						Pack.Position.TOP,
						false
				);

				Pack profile = Pack.readMetaAndCreate(info, new Pack.ResourcesSupplier() {
					@Override
					public PackResources openPrimary(PackLocationInfo var1) {
						return entry.getB();
					}

					@Override
					public PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
						// Don't support overlays in builtin res packs.
						return entry.getB();
					}
				}, resourceType, info2);
				consumer.accept(profile);
			}
		}
	}

	public static List<PreparableReloadListener> sort(PackType type, List<PreparableReloadListener> listeners) {
		if (type == null) {
			return listeners;
		}

		ResourceManagerHelperImpl instance = get(type);

		if (instance != null) {
			List<PreparableReloadListener> mutable = new ArrayList<>(listeners);
			instance.sort(mutable);
			return Collections.unmodifiableList(mutable);
		}

		return listeners;
	}

	protected void sort(List<PreparableReloadListener> listeners) {
		listeners.removeAll(addedListeners);

		// General rules:
		// - We *do not* touch the ordering of vanilla listeners. Ever.
		//   While dependency values are provided where possible, we cannot
		//   trust them 100%. Only code doesn't lie.
		// - We addReloadListener all custom listeners after vanilla listeners. Same reasons.

		final HolderLookup.Provider wrapperLookup = getWrapperLookup(listeners);
		List<IdentifiableResourceReloadListener> listenersToAdd = Lists.newArrayList();

		for (ListenerFactory addedListener : listenerFactories) {
			listenersToAdd.add(addedListener.get(wrapperLookup));
		}

		addedListeners.clear();
		addedListeners.addAll(listenersToAdd);

		Set<ResourceLocation> resolvedIds = new HashSet<>();

		for (PreparableReloadListener listener : listeners) {
			if (listener instanceof IdentifiableResourceReloadListener) {
				resolvedIds.add(((IdentifiableResourceReloadListener) listener).getNotebookId());
			}
		}

		int lastSize = -1;

		while (listeners.size() != lastSize) {
			lastSize = listeners.size();

			Iterator<IdentifiableResourceReloadListener> it = listenersToAdd.iterator();

			while (it.hasNext()) {
				IdentifiableResourceReloadListener listener = it.next();

				if (resolvedIds.containsAll(listener.getNotebookDependencies())) {
					resolvedIds.add(listener.getNotebookId());
					listeners.add(listener);
					it.remove();
				}
			}
		}

		for (IdentifiableResourceReloadListener listener : listenersToAdd) {
			LOGGER.warn("Could not resolve dependencies for listener: " + listener.getNotebookId() + "!");
		}
	}

	// A bit of a hack to get the registry, but it works.
	@Nullable
	private HolderLookup.Provider getWrapperLookup(List<PreparableReloadListener> listeners) {
		if (type == PackType.CLIENT_RESOURCES) {
			// We don't need the registry for client resources.
			return null;
		}

		for (PreparableReloadListener resourceReloader : listeners) {
			if (resourceReloader instanceof RecipeManager recipeManager) {
				return recipeManager.registries;
			}
		}

		throw new IllegalStateException("No RecipeManager found in listeners!");
	}

	@Override
	public void registerReloadListener(IdentifiableResourceReloadListener listener) {
		registerReloadListener(new SimpleResourceReloaderFactory(listener));
	}

	@Override
	public void registerReloadListener(ResourceLocation identifier, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory) {
		if (type == PackType.CLIENT_RESOURCES) {
			throw new IllegalArgumentException("Cannot register a registry listener for the client resource type!");
		}

		registerReloadListener(new RegistryResourceReloaderFactory(identifier, listenerFactory));
	}

	private void registerReloadListener(ListenerFactory factory) {
		if (!addedListenerIds.add(factory.id())) {
			LOGGER.warn("Tried to register resource reload listener " + factory.id() + " twice!");
			return;
		}

		if (!listenerFactories.add(factory)) {
			throw new RuntimeException("Listener with previously unknown ID " + factory.id() + " already in listener set!");
		}
	}

	private sealed interface ListenerFactory permits SimpleResourceReloaderFactory, RegistryResourceReloaderFactory {
		ResourceLocation id();

		IdentifiableResourceReloadListener get(HolderLookup.Provider registry);
	}

	private record SimpleResourceReloaderFactory(IdentifiableResourceReloadListener listener) implements ListenerFactory {
		@Override
		public ResourceLocation id() {
			return listener.getNotebookId();
		}

		@Override
		public IdentifiableResourceReloadListener get(HolderLookup.Provider registry) {
			return listener;
		}
	}

	private record RegistryResourceReloaderFactory(ResourceLocation id, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory) implements ListenerFactory {
		private RegistryResourceReloaderFactory {
			Objects.requireNonNull(listenerFactory);
		}

		@Override
		public IdentifiableResourceReloadListener get(HolderLookup.Provider registry) {
			final IdentifiableResourceReloadListener listener = listenerFactory.apply(registry);

			if (!id.equals(listener.getNotebookId())) {
				throw new IllegalStateException("Listener factory for " + id + " created a listener with ID " + listener.getNotebookId());
			}

			return listener;
		}
	}
}

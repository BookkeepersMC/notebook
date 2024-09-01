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

import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.HolderLookup;
import net.minecraft.resource.PackPosition;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackLocationInfo;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.notebook.api.resource.IdentifiableResourceReloadListener;
import com.bookkeepersmc.notebook.api.resource.ResourceManagerHelper;
import com.bookkeepersmc.notebook.api.resource.ResourcePackActivationType;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
	private static final Map<ResourceType, ResourceManagerHelperImpl> registryMap = new HashMap<>();
	private static final Set<Pair<Text, ModNioResourcePack>> builtinResourcePacks = new HashSet<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerHelperImpl.class);

	private final Set<Identifier> addedListenerIds = new HashSet<>();
	private final Set<ListenerFactory> listenerFactories = new LinkedHashSet<>();
	private final Set<IdentifiableResourceReloadListener> addedListeners = new LinkedHashSet<>();
	private final ResourceType type;

	private ResourceManagerHelperImpl(ResourceType type) {
		this.type = type;
	}

	public static ResourceManagerHelperImpl get(ResourceType type) {
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
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, Text, ResourcePackActivationType)
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 */
	public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, Text displayName, ResourcePackActivationType activationType) {
		// Assuming the mod has multiple paths, we simply "hope" that the  file separator is *not* different across them
		List<Path> paths = container.getRootPaths();
		String separator = paths.getFirst().getFileSystem().getSeparator();
		subPath = subPath.replace("/", separator);
		ModNioResourcePack resourcePack = ModNioResourcePack.create(id.toString(), container, subPath, ResourceType.CLIENT_RESOURCES, activationType, false);
		ModNioResourcePack dataPack = ModNioResourcePack.create(id.toString(), container, subPath, ResourceType.SERVER_DATA, activationType, false);
		if (resourcePack == null && dataPack == null) return false;

		if (resourcePack != null) {
			builtinResourcePacks.add(new Pair<>(displayName, resourcePack));
		}

		if (dataPack != null) {
			builtinResourcePacks.add(new Pair<>(displayName, dataPack));
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
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, Text, ResourcePackActivationType)
	 */
	public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, ResourcePackActivationType activationType) {
		return registerBuiltinResourcePack(id, subPath, container, Text.literal(id.getNamespace() + "/" + id.getPath()), activationType);
	}

	public static void registerBuiltinResourcePacks(ResourceType resourceType, Consumer<PackProfile> consumer) {
		// Loop through each registered built-in resource packs and add them if valid.
		for (Pair<Text, ModNioResourcePack> entry : builtinResourcePacks) {
			ModNioResourcePack pack = entry.getRight();

			// Add the built-in pack only if namespaces for the specified resource type are present.
			if (!pack.getNamespaces(resourceType).isEmpty()) {
				// Make the resource pack profile for built-in pack, should never be always enabled.
				PackLocationInfo info = new PackLocationInfo(
						entry.getRight().getName(),
						entry.getLeft(),
						new BuiltinModResourcePackSource(pack.getNotebookModMetadata().getName()),
						entry.getRight().getKnownPackInfo()
				);
				PackPosition info2 = new PackPosition(
						pack.getActivationType() == ResourcePackActivationType.ALWAYS_ENABLED,
						PackProfile.InsertionPosition.TOP,
						false
				);

				PackProfile profile = PackProfile.of(info, new PackProfile.PackFactory() {
					@Override
					public ResourcePack openPrimary(PackLocationInfo var1) {
						return entry.getRight();
					}

					@Override
					public ResourcePack open(PackLocationInfo var1, PackProfile.Metadata metadata) {
						// Don't support overlays in builtin res packs.
						return entry.getRight();
					}
				}, resourceType, info2);
				consumer.accept(profile);
			}
		}
	}

	public static List<ResourceReloader> sort(ResourceType type, List<ResourceReloader> listeners) {
		if (type == null) {
			return listeners;
		}

		ResourceManagerHelperImpl instance = get(type);

		if (instance != null) {
			List<ResourceReloader> mutable = new ArrayList<>(listeners);
			instance.sort(mutable);
			return Collections.unmodifiableList(mutable);
		}

		return listeners;
	}

	protected void sort(List<ResourceReloader> listeners) {
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

		Set<Identifier> resolvedIds = new HashSet<>();

		for (ResourceReloader listener : listeners) {
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
	private HolderLookup.Provider getWrapperLookup(List<ResourceReloader> listeners) {
		if (type == ResourceType.CLIENT_RESOURCES) {
			// We don't need the registry for client resources.
			return null;
		}

		for (ResourceReloader resourceReloader : listeners) {
			if (resourceReloader instanceof RecipeManager recipeManager) {
				return recipeManager.lookupProvider;
			}
		}

		throw new IllegalStateException("No RecipeManager found in listeners!");
	}

	@Override
	public void registerReloadListener(IdentifiableResourceReloadListener listener) {
		registerReloadListener(new SimpleResourceReloaderFactory(listener));
	}

	@Override
	public void registerReloadListener(Identifier identifier, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory) {
		if (type == ResourceType.CLIENT_RESOURCES) {
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
		Identifier id();

		IdentifiableResourceReloadListener get(HolderLookup.Provider registry);
	}

	private record SimpleResourceReloaderFactory(IdentifiableResourceReloadListener listener) implements ListenerFactory {
		@Override
		public Identifier id() {
			return listener.getNotebookId();
		}

		@Override
		public IdentifiableResourceReloadListener get(HolderLookup.Provider registry) {
			return listener;
		}
	}

	private record RegistryResourceReloaderFactory(Identifier id, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory) implements ListenerFactory {
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

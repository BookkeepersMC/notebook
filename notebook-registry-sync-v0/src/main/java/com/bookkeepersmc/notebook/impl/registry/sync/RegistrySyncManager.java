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
package com.bookkeepersmc.notebook.impl.registry.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.util.thread.BlockableEventLoop;

import com.bookkeepersmc.notebook.api.event.registry.RegistryAttribute;
import com.bookkeepersmc.notebook.api.event.registry.RegistryAttributeHolder;
import com.bookkeepersmc.notebook.api.networking.v1.ServerConfigurationNetworking;
import com.bookkeepersmc.notebook.impl.registry.sync.packet.DirectRegistryPacketHandler;
import com.bookkeepersmc.notebook.impl.registry.sync.packet.RegistryPacketHandler;

public final class RegistrySyncManager {
	public static final boolean DEBUG = Boolean.getBoolean("notebook.registry.debug");

	public static final DirectRegistryPacketHandler DIRECT_PACKET_HANDLER = new DirectRegistryPacketHandler();

	private static final Logger LOGGER = LoggerFactory.getLogger("notebookRegistrySync");
	private static final boolean DEBUG_WRITE_REGISTRY_DATA = Boolean.getBoolean("notebook.registry.debug.writeContentsAsCsv");

	//Set to true after vanilla's bootstrap has completed
	public static boolean postBootstrap = false;

	private RegistrySyncManager() { }

	public static void configureClient(ServerConfigurationPacketListenerImpl handler, MinecraftServer server) {
		if (!DEBUG && server.isSingleplayerOwner(handler.getOwner())) {
			// Dont send in singleplayer
			return;
		}

		if (!ServerConfigurationNetworking.canSend(handler, DIRECT_PACKET_HANDLER.getPacketId())) {
			// Don't send if the client cannot receive
			return;
		}

		final Map<ResourceLocation, Object2IntMap<ResourceLocation>> map = RegistrySyncManager.createAndPopulateRegistryMap();

		if (map == null) {
			// Don't send when there is nothing to map
			return;
		}

		handler.addTask(new SyncConfigurationTask(handler, map));
	}

	public record SyncConfigurationTask(
			ServerConfigurationPacketListenerImpl handler,
			Map<ResourceLocation, Object2IntMap<ResourceLocation>> map
	) implements ConfigurationTask {
		public static final Type KEY = new Type("notebook:registry/sync");

		@Override
		public void start(Consumer<Packet<?>> sender) {
			DIRECT_PACKET_HANDLER.sendPacket(payload -> handler.send(ServerConfigurationNetworking.createS2CPacket(payload)), map);
		}

		@Override
		public Type type() {
			return KEY;
		}
	}

	public static <T extends RegistryPacketHandler.RegistrySyncPayload> CompletableFuture<Boolean> receivePacket(BlockableEventLoop<?> executor, RegistryPacketHandler<T> handler, T payload, boolean accept) {
		handler.receivePayload(payload);

		if (!handler.isPacketFinished()) {
			return CompletableFuture.completedFuture(false);
		}

		if (DEBUG) {
			String handlerName = handler.getClass().getSimpleName();
			LOGGER.info("{} total packet: {}", handlerName, handler.getTotalPacketReceived());
			LOGGER.info("{} raw size: {}", handlerName, handler.getRawBufSize());
			LOGGER.info("{} deflated size: {}", handlerName, handler.getDeflatedBufSize());
		}

		Map<ResourceLocation, Object2IntMap<ResourceLocation>> map = handler.getSyncedRegistryMap();

		if (!accept) {
			return CompletableFuture.completedFuture(true);
		}

		return executor.submit(() -> {
			if (map == null) {
				throw new CompletionException(new RemapException("Received null map in sync packet!"));
			}

			try {
				apply(map, RemappableRegistry.RemapMode.REMOTE);
				return true;
			} catch (RemapException e) {
				throw new CompletionException(e);
			}
		});
	}

	/**
	 * Creates a {@link CompoundTag} used to sync the registry ids.
	 *
	 * @return a {@link CompoundTag} to sync, null when empty
	 */
	@Nullable
	public static Map<ResourceLocation, Object2IntMap<ResourceLocation>> createAndPopulateRegistryMap() {
		Map<ResourceLocation, Object2IntMap<ResourceLocation>> map = new LinkedHashMap<>();

		for (ResourceLocation registryId : BuiltInRegistries.REGISTRY.keySet()) {
			Registry registry = BuiltInRegistries.REGISTRY.get(registryId);

			if (DEBUG_WRITE_REGISTRY_DATA) {
				File location = new File(".fabric" + File.separatorChar + "debug" + File.separatorChar + "registry");
				boolean c = true;

				if (!location.exists()) {
					if (!location.mkdirs()) {
						LOGGER.warn("[notebook-registry-sync debug] Could not create " + location.getAbsolutePath() + " directory!");
						c = false;
					}
				}

				if (c && registry != null) {
					File file = new File(location, registryId.toString().replace(':', '.').replace('/', '.') + ".csv");

					try (FileOutputStream stream = new FileOutputStream(file)) {
						StringBuilder builder = new StringBuilder("Raw ID,String ID,Class Type\n");

						for (Object o : registry) {
							String classType = (o == null) ? "null" : o.getClass().getName();
							//noinspection unchecked
							ResourceLocation id = registry.getKey(o);
							if (id == null) continue;

							//noinspection unchecked
							int rawId = registry.getId(o);
							String stringId = id.toString();
							builder.append("\"").append(rawId).append("\",\"").append(stringId).append("\",\"").append(classType).append("\"\n");
						}

						stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						LOGGER.warn("[notebook-registry-sync debug] Could not write to " + file.getAbsolutePath() + "!", e);
					}
				}
			}

			RegistryAttributeHolder attributeHolder = RegistryAttributeHolder.get(registry.key());

			if (!attributeHolder.hasAttribute(RegistryAttribute.SYNCED)) {
				LOGGER.debug("Not syncing registry: {}", registryId);
				continue;
			}

			/*
			 * Dont do anything with vanilla registries on client sync.
			 *
			 * This will not sync IDs if a world has been previously modded, either from removed mods
			 * or a previous version of notebook registry sync.
			 */
			if (!attributeHolder.hasAttribute(RegistryAttribute.MODDED)) {
				LOGGER.debug("Skipping un-modded registry: " + registryId);
				continue;
			}

			LOGGER.debug("Syncing registry: " + registryId);

			if (registry instanceof RemappableRegistry) {
				Object2IntMap<ResourceLocation> idMap = new Object2IntLinkedOpenHashMap<>();
				IntSet rawIdsFound = DEBUG ? new IntOpenHashSet() : null;

				for (Object o : registry) {
					//noinspection unchecked
					ResourceLocation id = registry.getKey(o);
					if (id == null) continue;

					//noinspection unchecked
					int rawId = registry.getId(o);

					if (DEBUG) {
						if (registry.get(id) != o) {
							LOGGER.error("[notebook-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> string ID " + id + " -> object " + registry.get(id) + "!");
						}

						if (registry.byId(rawId) != o) {
							LOGGER.error("[notebook-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> integer ID " + rawId + " -> object " + registry.byId(rawId) + "!");
						}

						if (!rawIdsFound.add(rawId)) {
							LOGGER.error("[notebook-registry-sync] Inconsistency detected in " + registryId + ": multiple objects hold the raw ID " + rawId + " (this one is " + id + ")");
						}
					}

					idMap.put(id, rawId);
				}

				map.put(registryId, idMap);
			}
		}

		if (map.isEmpty()) {
			return null;
		}

		return map;
	}

	public static void apply(Map<ResourceLocation, Object2IntMap<ResourceLocation>> map, RemappableRegistry.RemapMode mode) throws RemapException {
		if (mode == RemappableRegistry.RemapMode.REMOTE) {
			checkRemoteRemap(map);
		}

		Set<ResourceLocation> containedRegistries = Sets.newHashSet(map.keySet());

		for (ResourceLocation registryId : BuiltInRegistries.REGISTRY.keySet()) {
			if (!containedRegistries.remove(registryId)) {
				continue;
			}

			Object2IntMap<ResourceLocation> registryMap = map.get(registryId);
			Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);

			RegistryAttributeHolder attributeHolder = RegistryAttributeHolder.get(registry.key());

			if (!attributeHolder.hasAttribute(RegistryAttribute.MODDED)) {
				LOGGER.debug("Not applying registry data to vanilla registry {}", registryId.toString());
				continue;
			}

			if (registry instanceof RemappableRegistry remappableRegistry) {
				remappableRegistry.remap(registryId.toString(), registryMap, mode);
			} else {
				throw new RemapException("Registry " + registryId + " is not remappable");
			}
		}

		if (!containedRegistries.isEmpty()) {
			LOGGER.warn("[notebook-registry-sync] Could not find the following registries: " + Joiner.on(", ").join(containedRegistries));
		}
	}

	@VisibleForTesting
	public static void checkRemoteRemap(Map<ResourceLocation, Object2IntMap<ResourceLocation>> map) throws RemapException {
		Map<ResourceLocation, List<ResourceLocation>> missingEntries = new HashMap<>();

		for (Map.Entry<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> entry : BuiltInRegistries.REGISTRY.entrySet()) {
			final Registry<?> registry = entry.getValue();
			final ResourceLocation registryId = entry.getKey().location();
			final Object2IntMap<ResourceLocation> remoteRegistry = map.get(registryId);

			if (remoteRegistry == null) {
				// Registry sync does not contain data for this registry, will print a warning when applying.
				continue;
			}

			for (ResourceLocation remoteId : remoteRegistry.keySet()) {
				if (!registry.containsKey(remoteId)) {
					// Found a registry entry from the server that is
					missingEntries.computeIfAbsent(registryId, i -> new ArrayList<>()).add(remoteId);
				}
			}
		}

		if (missingEntries.isEmpty()) {
			// All good :)
			return;
		}

		// Print out details to the log
		LOGGER.error("Received unknown remote registry entries from server");

		for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : missingEntries.entrySet()) {
			for (ResourceLocation identifier : entry.getValue()) {
				LOGGER.error("Registry entry ({}) is missing from local registry ({})", identifier, entry.getKey());
			}
		}

		// Create a nice user friendly error message.
		MutableComponent text = Component.empty();

		final int count = missingEntries.values().stream().mapToInt(List::size).sum();

		if (count == 1) {
			text = text.append(Component.translatable("notebook-registry-sync-v0.unknown-remote.title.singular"));
		} else {
			text = text.append(Component.translatable("notebook-registry-sync-v0.unknown-remote.title.plural", count));
		}

		text = text.append(Component.translatable("notebook-registry-sync-v0.unknown-remote.subtitle.1").withStyle(ChatFormatting.GREEN));
		text = text.append(Component.translatable("notebook-registry-sync-v0.unknown-remote.subtitle.2"));

		final int toDisplay = 4;
		// Get the distinct missing namespaces
		final List<String> namespaces = missingEntries.values().stream()
				.flatMap(List::stream)
				.map(ResourceLocation::getNamespace)
				.distinct()
				.sorted()
				.toList();

		for (int i = 0; i < Math.min(namespaces.size(), toDisplay); i++) {
			text = text.append(Component.literal(namespaces.get(i)).withStyle(ChatFormatting.YELLOW));
			text = text.append(CommonComponents.NEW_LINE);
		}

		if (namespaces.size() > toDisplay) {
			text = text.append(Component.translatable("notebook-registry-sync-v0.unknown-remote.footer", namespaces.size() - toDisplay));
		}

		throw new RemapException(text);
	}

	public static void unmap() throws RemapException {
		for (ResourceLocation registryId : BuiltInRegistries.REGISTRY.keySet()) {
			Registry registry = BuiltInRegistries.REGISTRY.get(registryId);

			if (registry instanceof RemappableRegistry) {
				((RemappableRegistry) registry).unmap(registryId.toString());
			}
		}
	}

	public static void bootstrapRegistries() {
		postBootstrap = true;
	}
}

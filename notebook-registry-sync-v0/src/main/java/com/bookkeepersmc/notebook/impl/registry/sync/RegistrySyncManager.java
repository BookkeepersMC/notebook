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

import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

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

	public static void configureClient(ServerConfigurationNetworkHandler handler, MinecraftServer server) {
		if (!DEBUG && server.isHost(handler.getHost())) {
			// Dont send in singleplayer
			return;
		}

		if (!ServerConfigurationNetworking.canSend(handler, DIRECT_PACKET_HANDLER.getPacketId())) {
			// Don't send if the client cannot receive
			return;
		}

		final Map<Identifier, Object2IntMap<Identifier>> map = RegistrySyncManager.createAndPopulateRegistryMap();

		if (map == null) {
			// Don't send when there is nothing to map
			return;
		}

		handler.addTask(new SyncConfigurationTask(handler, map));
	}

	public record SyncConfigurationTask(
			ServerConfigurationNetworkHandler handler,
			Map<Identifier, Object2IntMap<Identifier>> map
	) implements ConfigurationTask {
		public static final Type KEY = new Type("notebook:registry/sync");

		@Override
		public void start(Consumer<Packet<?>> sender) {
			DIRECT_PACKET_HANDLER.sendPacket(payload -> handler.send(ServerConfigurationNetworking.createS2CPacket(payload)), map);
		}

		@Override
		public Type getType() {
			return KEY;
		}
	}

	public static <T extends RegistryPacketHandler.RegistrySyncPayload> CompletableFuture<Boolean> receivePacket(ThreadExecutor<?> executor, RegistryPacketHandler<T> handler, T payload, boolean accept) {
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

		Map<Identifier, Object2IntMap<Identifier>> map = handler.getSyncedRegistryMap();

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
	 * Creates a {@link net.minecraft.nbt.NbtCompound} used to sync the registry ids.
	 *
	 * @return a {@link net.minecraft.nbt.NbtCompound} to sync, null when empty
	 */
	@Nullable
	public static Map<Identifier, Object2IntMap<Identifier>> createAndPopulateRegistryMap() {
		Map<Identifier, Object2IntMap<Identifier>> map = new LinkedHashMap<>();

		for (Identifier registryId : BuiltInRegistries.ROOT.getIds()) {
			Registry registry = BuiltInRegistries.ROOT.get(registryId);

			if (DEBUG_WRITE_REGISTRY_DATA) {
				File location = new File(".notebook" + File.separatorChar + "debug" + File.separatorChar + "registry");
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
							Identifier id = registry.getId(o);
							if (id == null) continue;

							//noinspection unchecked
							int rawId = registry.getRawId(o);
							String stringId = id.toString();
							builder.append("\"").append(rawId).append("\",\"").append(stringId).append("\",\"").append(classType).append("\"\n");
						}

						stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						LOGGER.warn("[notebook-registry-sync debug] Could not write to " + file.getAbsolutePath() + "!", e);
					}
				}
			}

			RegistryAttributeHolder attributeHolder = RegistryAttributeHolder.get(registry.getKey());

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
				Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
				IntSet rawIdsFound = DEBUG ? new IntOpenHashSet() : null;

				for (Object o : registry) {
					//noinspection unchecked
					Identifier id = registry.getId(o);
					if (id == null) continue;

					//noinspection unchecked
					int rawId = registry.getRawId(o);

					if (DEBUG) {
						if (registry.get(id) != o) {
							LOGGER.error("[notebook-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> string ID " + id + " -> object " + registry.get(id) + "!");
						}

						if (registry.get(rawId) != o) {
							LOGGER.error("[notebook-registry-sync] Inconsistency detected in " + registryId + ": object " + o + " -> integer ID " + rawId + " -> object " + registry.get(rawId) + "!");
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

	public static void apply(Map<Identifier, Object2IntMap<Identifier>> map, RemappableRegistry.RemapMode mode) throws RemapException {
		if (mode == RemappableRegistry.RemapMode.REMOTE) {
			checkRemoteRemap(map);
		}

		Set<Identifier> containedRegistries = Sets.newHashSet(map.keySet());

		for (Identifier registryId : BuiltInRegistries.ROOT.getIds()) {
			if (!containedRegistries.remove(registryId)) {
				continue;
			}

			Object2IntMap<Identifier> registryMap = map.get(registryId);
			Registry<?> registry = BuiltInRegistries.ROOT.get(registryId);

			RegistryAttributeHolder attributeHolder = RegistryAttributeHolder.get(registry.getKey());

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
	public static void checkRemoteRemap(Map<Identifier, Object2IntMap<Identifier>> map) throws RemapException {
		Map<Identifier, List<Identifier>> missingEntries = new HashMap<>();

		for (Map.Entry<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> entry : BuiltInRegistries.ROOT.getEntries()) {
			final Registry<?> registry = entry.getValue();
			final Identifier registryId = entry.getKey().getValue();
			final Object2IntMap<Identifier> remoteRegistry = map.get(registryId);

			if (remoteRegistry == null) {
				// Registry sync does not contain data for this registry, will print a warning when applying.
				continue;
			}

			for (Identifier remoteId : remoteRegistry.keySet()) {
				if (!registry.containsId(remoteId)) {
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

		for (Map.Entry<Identifier, List<Identifier>> entry : missingEntries.entrySet()) {
			for (Identifier identifier : entry.getValue()) {
				LOGGER.error("Registry entry ({}) is missing from local registry ({})", identifier, entry.getKey());
			}
		}

		// Create a nice user friendly error message.
		MutableText text = Text.empty();

		final int count = missingEntries.values().stream().mapToInt(List::size).sum();

		if (count == 1) {
			text = text.append(Text.translatable("notebook-registry-sync-v0.unknown-remote.title.singular"));
		} else {
			text = text.append(Text.translatable("notebook-registry-sync-v0.unknown-remote.title.plural", count));
		}

		text = text.append(Text.translatable("notebook-registry-sync-v0.unknown-remote.subtitle.1").formatted(Formatting.GREEN));
		text = text.append(Text.translatable("notebook-registry-sync-v0.unknown-remote.subtitle.2"));

		final int toDisplay = 4;
		// Get the distinct missing namespaces
		final List<String> namespaces = missingEntries.values().stream()
				.flatMap(List::stream)
				.map(Identifier::getNamespace)
				.distinct()
				.sorted()
				.toList();

		for (int i = 0; i < Math.min(namespaces.size(), toDisplay); i++) {
			text = text.append(Text.literal(namespaces.get(i)).formatted(Formatting.YELLOW));
			text = text.append(CommonTexts.LINE_BREAK);
		}

		if (namespaces.size() > toDisplay) {
			text = text.append(Text.translatable("notebook-registry-sync-v0.unknown-remote.footer", namespaces.size() - toDisplay));
		}

		throw new RemapException(text);
	}

	public static void unmap() throws RemapException {
		for (Identifier registryId : BuiltInRegistries.ROOT.getIds()) {
			Registry registry = BuiltInRegistries.ROOT.get(registryId);

			if (registry instanceof RemappableRegistry) {
				((RemappableRegistry) registry).unmap(registryId.toString());
			}
		}
	}

	public static void bootstrapRegistries() {
		postBootstrap = true;
	}
}

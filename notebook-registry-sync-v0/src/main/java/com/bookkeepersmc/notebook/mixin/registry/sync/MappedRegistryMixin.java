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
package com.bookkeepersmc.notebook.mixin.registry.sync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Holder;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistrationInfo;
import net.minecraft.registry.Registry;
import net.minecraft.registry.ResourceKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;
import com.bookkeepersmc.notebook.api.event.registry.RegistryAttribute;
import com.bookkeepersmc.notebook.api.event.registry.RegistryAttributeHolder;
import com.bookkeepersmc.notebook.api.event.registry.RegistryEntryAddedCallback;
import com.bookkeepersmc.notebook.api.event.registry.RegistryIdRemapCallback;
import com.bookkeepersmc.notebook.impl.registry.sync.ListenableRegistry;
import com.bookkeepersmc.notebook.impl.registry.sync.RegistrySyncManager;
import com.bookkeepersmc.notebook.impl.registry.sync.RemapException;
import com.bookkeepersmc.notebook.impl.registry.sync.RemapStateImpl;
import com.bookkeepersmc.notebook.impl.registry.sync.RemappableRegistry;

@Mixin(SimpleRegistry.class)
public abstract class MappedRegistryMixin<T> implements MutableRegistry<T>, RemappableRegistry, ListenableRegistry<T> {
	@Unique
	private static final Set<String> VANILLA_NAMESPACES = Set.of("minecraft", "brigadier");

	@Shadow
	@Final
	private ObjectList<Holder.Reference<T>> rawIdToEntry;
	@Shadow
	@Final
	private Reference2IntMap<T> entryToRawId;
	@Shadow
	@Final
	private Map<Identifier, Holder.Reference<T>> byId;
	@Shadow
	@Final
	private Map<ResourceKey<T>, Holder.Reference<T>> byKey;

	@Shadow
	public abstract Optional<ResourceKey<T>> getKey(T entry);

	@Shadow
	public abstract @Nullable T get(@Nullable Identifier id);

	@Shadow
	public abstract ResourceKey<? extends Registry<T>> getKey();

	@Shadow
	public abstract int getRawId(@Nullable T entry);

	@Unique
	private static final Logger NOTEBOOK_LOGGER = LoggerFactory.getLogger(MappedRegistryMixin.class);

	@Unique
	private Event<RegistryEntryAddedCallback<T>> notebook_addObjectEvent;

	@Unique
	private Event<RegistryIdRemapCallback<T>> notebook_postRemapEvent;

	@Unique
	private Object2IntMap<Identifier> notebook_prevIndexedEntries;
	@Unique
	private BiMap<Identifier, Holder.Reference<T>> notebook_prevEntries;

	@Override
	public Event<RegistryEntryAddedCallback<T>> notebook_getAddObjectEvent() {
		return notebook_addObjectEvent;
	}

	@Override
	public Event<RegistryIdRemapCallback<T>> notebook_getRemapEvent() {
		return notebook_postRemapEvent;
	}

	@Inject(method = "<init>(Lnet/minecraft/registry/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("RETURN"))
	private void init(ResourceKey key, Lifecycle lifecycle, boolean intrusive, CallbackInfo ci) {
		notebook_addObjectEvent = EventFactory.createArrayBacked(RegistryEntryAddedCallback.class,
				(callbacks) -> (rawId, id, object) -> {
					for (RegistryEntryAddedCallback<T> callback : callbacks) {
						callback.onEntryAdded(rawId, id, object);
					}
				}
		);
		notebook_postRemapEvent = EventFactory.createArrayBacked(RegistryIdRemapCallback.class,
				(callbacks) -> (a) -> {
					for (RegistryIdRemapCallback<T> callback : callbacks) {
						callback.onRemap(a);
					}
				}
		);
	}

	@Unique
	private void onChange(ResourceKey<T> registryKey) {
		if (RegistrySyncManager.postBootstrap || !VANILLA_NAMESPACES.contains(registryKey.getValue().getNamespace())) {
			RegistryAttributeHolder holder = RegistryAttributeHolder.get(getKey());

			if (!holder.hasAttribute(RegistryAttribute.MODDED)) {
				Identifier id = getKey().getValue();
				NOTEBOOK_LOGGER.debug("Registry {} has been marked as modded, registry entry {} was changed", id, registryKey.getValue());
				RegistryAttributeHolder.get(getKey()).addAttribute(RegistryAttribute.MODDED);
			}
		}
	}

	@Inject(method = "register", at = @At("RETURN"))
	private void set(ResourceKey<T> key, T entry, RegistrationInfo arg, CallbackInfoReturnable<Holder.Reference<T>> info) {
		// We need to restore the 1.19 behavior of binding the value to references immediately.
		// Unfrozen registries cannot be interacted with otherwise, because the references would throw when
		// trying to access their values.
		info.getReturnValue().setValue(entry);

		notebook_addObjectEvent.invoker().onEntryAdded(entryToRawId.getInt(entry), key.getValue(), entry);
		onChange(key);
	}

	@Override
	public void remap(String name, Object2IntMap<Identifier> remoteIndexedEntries, RemapMode mode) throws RemapException {
		// Throw on invalid conditions.
		switch (mode) {
			case AUTHORITATIVE:
				break;
			case REMOTE: {
				List<String> strings = null;

				for (Identifier remoteId : remoteIndexedEntries.keySet()) {
					if (!byId.containsKey(remoteId)) {
						if (strings == null) {
							strings = new ArrayList<>();
						}

						strings.add(" - " + remoteId);
					}
				}

				if (strings != null) {
					StringBuilder builder = new StringBuilder("Received ID map for " + name + " contains IDs unknown to the receiver!");

					for (String s : strings) {
						builder.append('\n').append(s);
					}

					throw new RemapException(builder.toString());
				}

				break;
			}
			case EXACT: {
				if (!byId.keySet().equals(remoteIndexedEntries.keySet())) {
					List<String> strings = new ArrayList<>();

					for (Identifier remoteId : remoteIndexedEntries.keySet()) {
						if (!byId.containsKey(remoteId)) {
							strings.add(" - " + remoteId + " (missing on local)");
						}
					}

					for (Identifier localId : getIds()) {
						if (!remoteIndexedEntries.containsKey(localId)) {
							strings.add(" - " + localId + " (missing on remote)");
						}
					}

					StringBuilder builder = new StringBuilder("Local and remote ID sets for " + name + " do not match!");

					for (String s : strings) {
						builder.append('\n').append(s);
					}

					throw new RemapException(builder.toString());
				}

				break;
			}
		}

		// Make a copy of the previous maps.
		// For now, only one is necessary - on an integrated server scenario,
		// AUTHORITATIVE == CLIENT, which is fine.
		// The reason we preserve the first one is because it contains the
		// vanilla order of IDs before mods, which is crucial for vanilla server
		// compatibility.
		if (notebook_prevIndexedEntries == null) {
			notebook_prevIndexedEntries = new Object2IntOpenHashMap<>();
			notebook_prevEntries = HashBiMap.create(byId);

			for (T o : this) {
				notebook_prevIndexedEntries.put(getId(o), getRawId(o));
			}
		}

		Int2ObjectMap<Identifier> oldIdMap = new Int2ObjectOpenHashMap<>();

		for (T o : this) {
			oldIdMap.put(getRawId(o), getId(o));
		}

		// If we're AUTHORITATIVE, we append entries which only exist on the
		// local side to the new entry list. For REMOTE, we instead drop them.
		switch (mode) {
			case AUTHORITATIVE: {
				int maxValue = 0;

				Object2IntMap<Identifier> oldRemoteIndexedEntries = remoteIndexedEntries;
				remoteIndexedEntries = new Object2IntOpenHashMap<>();

				for (Identifier id : oldRemoteIndexedEntries.keySet()) {
					int v = oldRemoteIndexedEntries.getInt(id);
					remoteIndexedEntries.put(id, v);
					if (v > maxValue) maxValue = v;
				}

				for (Identifier id : getIds()) {
					if (!remoteIndexedEntries.containsKey(id)) {
						NOTEBOOK_LOGGER.warn("Adding " + id + " to saved/remote registry.");
						remoteIndexedEntries.put(id, ++maxValue);
					}
				}

				break;
			}
			case REMOTE: {
				int maxId = -1;

				for (Identifier id : getIds()) {
					if (!remoteIndexedEntries.containsKey(id)) {
						if (maxId < 0) {
							for (int value : remoteIndexedEntries.values()) {
								if (value > maxId) {
									maxId = value;
								}
							}
						}

						if (maxId < 0) {
							throw new RemapException("Failed to assign new id to client only registry entry");
						}

						maxId++;

						NOTEBOOK_LOGGER.debug("An ID for {} was not sent by the server, assuming client only registry entry and assigning a new id ({}) in {}", id.toString(), maxId, getKey().getValue().toString());
						remoteIndexedEntries.put(id, maxId);
					}
				}

				break;
			}
		}

		Int2IntMap idMap = new Int2IntOpenHashMap();

		for (int i = 0; i < rawIdToEntry.size(); i++) {
			Holder.Reference<T> reference = rawIdToEntry.get(i);

			// Unused id, can happen if there are holes in the registry.
			if (reference == null) {
				throw new RemapException("Unused id " + i + " in registry " + getKey().getValue());
			}

			Identifier id = reference.getRegistryKey().getValue();

			// see above note
			if (remoteIndexedEntries.containsKey(id)) {
				idMap.put(i, remoteIndexedEntries.getInt(id));
			}
		}

		// entries was handled above, if it was necessary.
		rawIdToEntry.clear();
		entryToRawId.clear();

		List<Identifier> orderedRemoteEntries = new ArrayList<>(remoteIndexedEntries.keySet());
		orderedRemoteEntries.sort(Comparator.comparingInt(remoteIndexedEntries::getInt));

		for (Identifier identifier : orderedRemoteEntries) {
			int id = remoteIndexedEntries.getInt(identifier);
			Holder.Reference<T> object = byId.get(identifier);

			// Warn if an object is missing from the local registry.
			// This should only happen in AUTHORITATIVE mode, and as such we
			// throw an exception otherwise.
			if (object == null) {
				if (mode != RemapMode.AUTHORITATIVE) {
					throw new RemapException(identifier + " missing from registry, but requested!");
				} else {
					NOTEBOOK_LOGGER.warn(identifier + " missing from registry, but requested!");
				}

				continue;
			}

			// Add the new object
			rawIdToEntry.size(Math.max(this.rawIdToEntry.size(), id + 1));
			assert rawIdToEntry.get(id) == null;
			rawIdToEntry.set(id, object);
			entryToRawId.put(object.getValue(), id);
		}

		notebook_getRemapEvent().invoker().onRemap(new RemapStateImpl<>(this, oldIdMap, idMap));
	}

	@Override
	public void unmap(String name) throws RemapException {
		if (notebook_prevIndexedEntries != null) {
			List<Identifier> addedIds = new ArrayList<>();

			// Emit AddObject events for previously culled objects.
			for (Identifier id : notebook_prevEntries.keySet()) {
				if (!byId.containsKey(id)) {
					assert notebook_prevIndexedEntries.containsKey(id);
					addedIds.add(id);
				}
			}

			byId.clear();
			byKey.clear();

			byId.putAll(notebook_prevEntries);

			for (Map.Entry<Identifier, Holder.Reference<T>> entry : notebook_prevEntries.entrySet()) {
				ResourceKey<T> entryKey = ResourceKey.of(getKey(), entry.getKey());
				byKey.put(entryKey, entry.getValue());
			}

			remap(name, notebook_prevIndexedEntries, RemapMode.AUTHORITATIVE);

			for (Identifier id : addedIds) {
				notebook_getAddObjectEvent().invoker().onEntryAdded(entryToRawId.getInt(byId.get(id)), id, get(id));
			}

			notebook_prevIndexedEntries = null;
			notebook_prevEntries = null;
		}
	}
}

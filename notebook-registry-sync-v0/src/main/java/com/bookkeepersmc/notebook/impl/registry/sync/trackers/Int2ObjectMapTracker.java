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
package com.bookkeepersmc.notebook.impl.registry.sync.trackers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.event.registry.RegistryEntryAddedCallback;
import com.bookkeepersmc.notebook.api.event.registry.RegistryIdRemapCallback;

public class Int2ObjectMapTracker<V, OV> implements RegistryEntryAddedCallback<V>, RegistryIdRemapCallback<V> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Int2ObjectMapTracker.class);
	private final String name;
	private final Int2ObjectMap<OV> mappers;
	private Map<ResourceLocation, OV> removedMapperCache = new HashMap<>();

	private Int2ObjectMapTracker(String name, Int2ObjectMap<OV> mappers) {
		this.name = name;
		this.mappers = mappers;
	}

	public static <V, OV> void register(Registry<V> registry, String name, Int2ObjectMap<OV> mappers) {
		Int2ObjectMapTracker<V, OV> updater = new Int2ObjectMapTracker<>(name, mappers);
		RegistryEntryAddedCallback.event(registry).register(updater);
		RegistryIdRemapCallback.event(registry).register(updater);
	}

	@Override
	public void onEntryAdded(int rawId, ResourceLocation id, V object) {
		if (removedMapperCache.containsKey(id)) {
			mappers.put(rawId, removedMapperCache.get(id));
		}
	}

	@Override
	public void onRemap(RemapState<V> state) {
		Int2ObjectMap<OV> oldMappers = new Int2ObjectOpenHashMap<>(mappers);
		Int2IntMap remapMap = state.getSyncIdChangeMap();
		List<String> errors = null;

		mappers.clear();

		for (int i : oldMappers.keySet()) {
			int newI = remapMap.getOrDefault(i, Integer.MIN_VALUE);

			if (newI >= 0) {
				if (mappers.containsKey(newI)) {
					if (errors == null) {
						errors = new ArrayList<>();
					}

					errors.add(" - Map contained two equal IDs " + newI + " (" + state.getIdFromOld(i) + "/" + i + " -> " + state.getIdFromNew(newI) + "/" + newI + ")!");
				} else {
					mappers.put(newI, oldMappers.get(i));
				}
			} else {
				LOGGER.warn("[notebook-registry-sync] Int2ObjectMap " + name + " is dropping mapping for integer ID " + i + " (" + state.getIdFromOld(i) + ") - should not happen!");
				removedMapperCache.put(state.getIdFromOld(i), oldMappers.get(i));
			}
		}

		if (errors != null) {
			throw new RuntimeException("Errors while remapping Int2ObjectMap " + name + " found:\n" + Joiner.on('\n').join(errors));
		}
	}
}

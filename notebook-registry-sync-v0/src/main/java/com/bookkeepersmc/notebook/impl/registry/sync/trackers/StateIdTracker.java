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

import java.util.Collection;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.event.registry.RegistryEntryAddedCallback;
import com.bookkeepersmc.notebook.api.event.registry.RegistryIdRemapCallback;
import com.bookkeepersmc.notebook.impl.registry.sync.RemovableIdList;

public final class StateIdTracker<T, S> implements RegistryIdRemapCallback<T>, RegistryEntryAddedCallback<T> {
	private final Logger logger = LoggerFactory.getLogger(StateIdTracker.class);
	private final Registry<T> registry;
	private final IdMapper<S> stateList;
	private final Function<T, Collection<S>> stateGetter;
	private int currentHighestId = 0;

	public static <T, S> void register(Registry<T> registry, IdMapper<S> stateList, Function<T, Collection<S>> stateGetter) {
		StateIdTracker<T, S> tracker = new StateIdTracker<>(registry, stateList, stateGetter);
		RegistryEntryAddedCallback.event(registry).register(tracker);
		RegistryIdRemapCallback.event(registry).register(tracker);
	}

	private StateIdTracker(Registry<T> registry, IdMapper<S> stateList, Function<T, Collection<S>> stateGetter) {
		this.registry = registry;
		this.stateList = stateList;
		this.stateGetter = stateGetter;

		recalcHighestId();
	}

	@Override
	public void onEntryAdded(int rawId, ResourceLocation id, T object) {
		if (rawId == currentHighestId + 1) {
			stateGetter.apply(object).forEach(stateList::add);
			currentHighestId = rawId;
		} else {
			logger.debug("[notebook-registry-sync] Non-sequential RegistryEntryAddedCallback for " + object.getClass().getSimpleName() + " ID tracker (at " + id + "), forcing state map recalculation...");
			recalcStateMap();
		}
	}

	@Override
	public void onRemap(RemapState<T> state) {
		recalcStateMap();
	}

	private void recalcStateMap() {
		((RemovableIdList<?>) stateList).notebook_clear();

		Int2ObjectMap<T> sortedBlocks = new Int2ObjectRBTreeMap<>();

		currentHighestId = 0;
		registry.forEach((t) -> {
			int rawId = registry.getId(t);
			currentHighestId = Math.max(currentHighestId, rawId);
			sortedBlocks.put(rawId, t);
		});

		for (T b : sortedBlocks.values()) {
			stateGetter.apply(b).forEach(stateList::add);
		}
	}

	private void recalcHighestId() {
		currentHighestId = 0;

		for (T object : registry) {
			currentHighestId = Math.max(currentHighestId, registry.getId(object));
		}
	}
}

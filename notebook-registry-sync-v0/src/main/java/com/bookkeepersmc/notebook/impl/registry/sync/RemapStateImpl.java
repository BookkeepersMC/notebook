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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.event.registry.RegistryIdRemapCallback;

public class RemapStateImpl<T> implements RegistryIdRemapCallback.RemapState<T> {
	private final Int2IntMap syncIdChangeMap;
	private final Int2ObjectMap<Identifier> oldIdMap;
	private final Int2ObjectMap<Identifier> newIdMap;

	public RemapStateImpl(Registry<T> registry, Int2ObjectMap<Identifier> oldIdMap, Int2IntMap rawIdChangeMap) {
		this.syncIdChangeMap = rawIdChangeMap;
		this.oldIdMap = oldIdMap;
		this.newIdMap = new Int2ObjectOpenHashMap<>();

		for (Int2IntMap.Entry entry : rawIdChangeMap.int2IntEntrySet()) {
			Identifier id = registry.getId(registry.get(entry.getIntValue()));
			newIdMap.put(entry.getIntValue(), id);
		}
	}

	@Override
	public Int2IntMap getSyncIdChangeMap() {
		return syncIdChangeMap;
	}

	@Override
	public Identifier getIdFromOld(int oldSyncId) {
		return oldIdMap.get(oldSyncId);
	}

	@Override
	public Identifier getIdFromNew(int newSyncId) {
		return newIdMap.get(newSyncId);
	}
}

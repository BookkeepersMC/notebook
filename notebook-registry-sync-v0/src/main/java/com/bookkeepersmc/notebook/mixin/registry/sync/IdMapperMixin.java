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
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.IdMapper;

import com.bookkeepersmc.notebook.impl.registry.sync.RemovableIdList;

@Mixin(IdMapper.class)
public class IdMapperMixin<T> implements RemovableIdList<T> {
	@Shadow
	private int nextId;
	@Final
	@Shadow
	private Reference2IntMap<T> tToId;
	@Final
	@Shadow
	private List<T> idToT;

	@Override
	public void notebook_clear() {
		nextId = 0;
		tToId.clear();
		idToT.clear();
	}

	@Unique
	private void notebook_removeInner(T o) {
		int value = tToId.removeInt(o);
		idToT.set(value, null);

		while (nextId > 1 && idToT.get(nextId - 1) == null) {
			nextId--;
		}
	}

	@Override
	public void notebook_remove(T o) {
		if (tToId.containsKey(o)) {
			notebook_removeInner(o);
		}
	}

	@Override
	public void notebook_removeId(int i) {
		List<T> removals = new ArrayList<>();

		for (T o : tToId.keySet()) {
			int j = tToId.getInt(o);

			if (i == j) {
				removals.add(o);
			}
		}

		removals.forEach(this::notebook_removeInner);
	}

	@Override
	public void notebook_remapId(int from, int to) {
		notebook_remapIds(Int2IntMaps.singleton(from, to));
	}

	@Override
	public void notebook_remapIds(Int2IntMap map) {
		// remap idMap
		tToId.replaceAll((a, b) -> map.get((int) b));

		// remap list
		nextId = 0;
		List<T> oldList = new ArrayList<>(idToT);
		idToT.clear();

		for (int k = 0; k < oldList.size(); k++) {
			T o = oldList.get(k);

			if (o != null) {
				int i = map.getOrDefault(k, k);

				while (idToT.size() <= i) {
					idToT.add(null);
				}

				idToT.set(i, o);

				if (nextId <= i) {
					nextId = i + 1;
				}
			}
		}
	}
}

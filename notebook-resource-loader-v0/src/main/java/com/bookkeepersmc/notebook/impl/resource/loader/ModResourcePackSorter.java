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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.bookkeepersmc.notebook.api.resource.ModResourcePack;
import com.bookkeepersmc.notebook.impl.base.toposport.NodeSorting;
import com.bookkeepersmc.notebook.impl.base.toposport.SortableNode;

public class ModResourcePackSorter {
	private final Object lock = new Object();
	private ModResourcePack[] packs;

	private final Map<String, LoadPhaseData> phases = new LinkedHashMap<>();

	private final List<LoadPhaseData> sortedPhases = new ArrayList<>();

	ModResourcePackSorter() {
		this.packs = new ModResourcePack[0];
	}

	public void appendPacks(List<ModResourcePack> packs) {
		packs.addAll(Arrays.asList(this.packs));
	}

	public void addPack(ModResourcePack pack) {
		Objects.requireNonNull(pack, "Can't register a null pack");

		String modId = pack.getName();
		Objects.requireNonNull(modId, "Can't register a pack without a mod id");

		synchronized (lock) {
			getOrCreatePhase(modId, true).addPack(pack);
			rebuildPackList(packs.length + 1);
		}
	}

	private LoadPhaseData getOrCreatePhase(String id, boolean sortIfCreate) {
		LoadPhaseData phase = phases.get(id);

		if (phase == null) {
			phase = new LoadPhaseData(id);
			phases.put(id, phase);
			sortedPhases.add(phase);

			if (sortIfCreate) {
				NodeSorting.sort(sortedPhases, "mod resource packs", Comparator.comparing(data -> data.modId));
			}
		}

		return phase;
	}

	private void rebuildPackList(int newLength) {
		// Rebuild pack list.
		if (sortedPhases.size() == 1) {
			packs = sortedPhases.getFirst().packs;
		} else {
			ModResourcePack[] newHandlers = new ModResourcePack[newLength];
			int newHandlersIndex = 0;

			for (LoadPhaseData existingPhase : sortedPhases) {
				int length = existingPhase.packs.length;
				System.arraycopy(existingPhase.packs, 0, newHandlers, newHandlersIndex, length);
				newHandlersIndex += length;
			}

			packs = newHandlers;
		}
	}

	public void addLoadOrdering(String firstPhase, String secondPhase, boolean before) {
		Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
		Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
		if (firstPhase.equals(secondPhase)) throw new IllegalArgumentException("Tried to add a phase that depends on itself.");

		synchronized (lock) {
			LoadPhaseData first = getOrCreatePhase(firstPhase, false);
			LoadPhaseData second = getOrCreatePhase(secondPhase, false);

			if (before) {
				LoadPhaseData.link(first, second);
			} else {
				LoadPhaseData.link(second, first);
			}

			NodeSorting.sort(this.sortedPhases, "event phases", Comparator.comparing(data -> data.modId));
			rebuildPackList(packs.length);
		}
	}

	public static class LoadPhaseData extends SortableNode<LoadPhaseData> {
		final String modId;
		ModResourcePack[] packs;

		LoadPhaseData(String modId) {
			this.modId = modId;
			this.packs = new ModResourcePack[0];
		}

		void addPack(ModResourcePack pack) {
			int oldLength = packs.length;
			packs = Arrays.copyOf(packs, oldLength + 1);
			packs[oldLength] = pack;
		}

		@Override
		protected String getDescription() {
			return modId;
		}
	}
}

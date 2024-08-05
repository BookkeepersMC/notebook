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
package com.bookkeepersmc.notebook.impl.recipe;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ShapelessMatch {
	private final int[] match;

	private final BitSet bitSet;

	private ShapelessMatch(int size) {
		match = new int[size];
		bitSet = new BitSet(size * (size+1));
	}

	private boolean augment(int l) {
		if (bitSet.get(l)) return false;
		bitSet.set(l);

		for (int r = 0; r < match.length; ++r) {
			if (bitSet.get(match.length + l * match.length + r)) {
				if (match[r] == -1 || augment(match[r])) {
					match[r] = l;
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isMatch(List<ItemStack> stacks, List<Ingredient> ingredients) {
		if (stacks.size() != ingredients.size()) {
			return false;
		}

		ShapelessMatch m = new ShapelessMatch(ingredients.size());

		// Build stack -> ingredient bipartite graph
		for (int i = 0; i < stacks.size(); ++i) {
			ItemStack stack = stacks.get(i);

			for (int j = 0; j < ingredients.size(); ++j) {
				if (ingredients.get(j).test(stack)) {
					m.bitSet.set((i + 1) * m.match.length + j);
				}
			}
		}

		// Init matches to -1 (no match)
		Arrays.fill(m.match, -1);

		// Try to find an augmenting path for each stack
		for (int i = 0; i < ingredients.size(); ++i) {
			if (!m.augment(i)) {
				return false;
			}

			m.bitSet.set(0, m.match.length, false);
		}

		return true;
	}
}

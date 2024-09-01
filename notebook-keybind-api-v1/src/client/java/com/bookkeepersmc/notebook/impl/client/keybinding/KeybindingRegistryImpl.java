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
package com.bookkeepersmc.notebook.impl.client.keybinding;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.option.KeyBind;

import com.bookkeepersmc.notebook.mixin.client.keybind.KeyMappingAccessor;

public final class KeybindingRegistryImpl {
	private static final List<KeyBind> MODDED_KEYBINDS = new ReferenceArrayList<>();

	private KeybindingRegistryImpl() {
	}

	private static Map<String, Integer> getCategoryMap() {
		return KeyMappingAccessor.notebook_getCategoryMap();
	}

	public static boolean addCategory(String categoryTranslationKey) {
		Map<String, Integer> map = getCategoryMap();

		if (map.containsKey(categoryTranslationKey)) {
			return false;
		}

		Optional<Integer> largest = map.values().stream().max(Integer::compareTo);
		int largestInt = largest.orElse(0);
		map.put(categoryTranslationKey, largestInt + 1);
		return true;
	}

	public static KeyBind registerKeybind(KeyBind binding) {
		if (Minecraft.getInstance().options != null) {
			throw new IllegalStateException("GameOptions has already been initialised");
		}

		for (KeyBind existingKeyBindings : MODDED_KEYBINDS) {
			if (existingKeyBindings == binding) {
				throw new IllegalArgumentException("Attempted to register a key binding twice: " + binding.getKeyName());
			} else if (existingKeyBindings.getKeyName().equals(binding.getKeyName())) {
				throw new IllegalArgumentException("Attempted to register two key bindings with equal ID: " + binding.getKeyName() + "!");
			}
		}

		// This will do nothing if the category already exists.
		addCategory(binding.getCategory());
		MODDED_KEYBINDS.add(binding);
		return binding;
	}

	public static KeyBind[] process(KeyBind[] keysAll) {
		List<KeyBind> newKeysAll = Lists.newArrayList(keysAll);
		newKeysAll.removeAll(MODDED_KEYBINDS);
		newKeysAll.addAll(MODDED_KEYBINDS);
		return newKeysAll.toArray(new KeyBind[0]);
	}
}

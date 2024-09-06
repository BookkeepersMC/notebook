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
package com.bookkeepersmc.notebook.impl.client.rendering;

import java.util.HashMap;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.BuiltInRegistries;

import com.bookkeepersmc.notebook.api.client.rendering.v1.ArmorRenderer;

public class ArmorRendererRegistryImpl {
	private static final HashMap<Item, ArmorRenderer> RENDERERS = new HashMap<>();

	public static void register(ArmorRenderer renderer, ItemConvertible... items) {
		Objects.requireNonNull(renderer, "renderer is null");

		if (items.length == 0) {
			throw new IllegalArgumentException("Armor renderer registered for no item");
		}

		for (ItemConvertible item : items) {
			Objects.requireNonNull(item.asItem(), "armor item is null");

			if (RENDERERS.putIfAbsent(item.asItem(), renderer) != null) {
				throw new IllegalArgumentException("Custom armor renderer already exists for " + BuiltInRegistries.ITEM.getId(item.asItem()));
			}
		}
	}

	@Nullable
	public static ArmorRenderer get(Item item) {
		return RENDERERS.get(item);
	}
}

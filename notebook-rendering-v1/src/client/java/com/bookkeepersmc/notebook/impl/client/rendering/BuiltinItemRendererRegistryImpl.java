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
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.BuiltInRegistries;

import com.bookkeepersmc.notebook.api.client.rendering.v1.BuiltinItemRendererRegistry;

public final class BuiltinItemRendererRegistryImpl implements BuiltinItemRendererRegistry {
	private static final Map<Item, DynamicItemRenderer> RENDERERS = new HashMap<>();

	public BuiltinItemRendererRegistryImpl() {
	}

	@Override
	public void register(ItemConvertible item, DynamicItemRenderer renderer) {
		Objects.requireNonNull(item, "item is null");
		Objects.requireNonNull(item.asItem(), "item is null");
		Objects.requireNonNull(renderer, "renderer is null");

		if (RENDERERS.putIfAbsent(item.asItem(), renderer) != null) {
			throw new IllegalArgumentException("Item " + BuiltInRegistries.ITEM.getId(item.asItem()) + " already has a builtin renderer!");
		}
	}

	@Override
	@Nullable
	public DynamicItemRenderer get(ItemConvertible item) {
		Objects.requireNonNull(item.asItem(), "item is null");

		return RENDERERS.get(item.asItem());
	}
}

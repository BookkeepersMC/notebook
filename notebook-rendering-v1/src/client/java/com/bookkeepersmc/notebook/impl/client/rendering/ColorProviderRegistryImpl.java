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

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.ItemConvertible;

import com.bookkeepersmc.notebook.api.client.rendering.v1.ColorProviderRegistry;

public abstract class ColorProviderRegistryImpl<T, P, U> implements ColorProviderRegistry<T, P> {
	public static final ColorProviderRegistryImpl<Block, BlockColorProvider, BlockColors> BLOCK = new ColorProviderRegistryImpl<Block, BlockColorProvider, BlockColors>() {
		@Override
		void registerUnderlying(BlockColors map, BlockColorProvider mapper, Block block) {
			map.registerColorProvider(mapper, block);
		}
	};

	public static final ColorProviderRegistryImpl<ItemConvertible, ItemColorProvider, ItemColors> ITEM = new ColorProviderRegistryImpl<ItemConvertible, ItemColorProvider, ItemColors>() {
		@Override
		void registerUnderlying(ItemColors map, ItemColorProvider mapper, ItemConvertible block) {
			map.register(mapper, block);
		}
	};

	private U colorMap;
	private Map<T, P> tempMappers = new IdentityHashMap<>();

	abstract void registerUnderlying(U colorMap, P provider, T objects);

	public void initialize(U colorMap) {
		if (this.colorMap != null) {
			if (this.colorMap != colorMap) throw new IllegalStateException("Cannot set colorMap twice");
			return;
		}

		this.colorMap = colorMap;

		for (Map.Entry<T, P> mappers : tempMappers.entrySet()) {
			registerUnderlying(colorMap, mappers.getValue(), mappers.getKey());
		}

		tempMappers = null;
	}

	@Override
	@SafeVarargs
	public final void register(P provider, T... objects) {
		if (colorMap != null) {
			for (T object : objects) registerUnderlying(colorMap, provider, object);
		} else {
			for (T object : objects) tempMappers.put(object, provider);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public P get(T object) {
		return colorMap == null ? null : ((ColorMapperHolder<T, P>) colorMap).get(object);
	}

	public interface ColorMapperHolder<T, Provider> {
		Provider get(T item);
	}
}

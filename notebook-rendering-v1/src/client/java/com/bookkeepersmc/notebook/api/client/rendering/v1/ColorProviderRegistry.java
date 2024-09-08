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
package com.bookkeepersmc.notebook.api.client.rendering.v1;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.ItemConvertible;

import com.bookkeepersmc.notebook.impl.client.rendering.ColorProviderRegistryImpl;

public interface ColorProviderRegistry<T, P> {
	ColorProviderRegistry<ItemConvertible, ItemColorProvider> ITEM = ColorProviderRegistryImpl.ITEM;

	ColorProviderRegistry<Block, BlockColorProvider> BLOCK = ColorProviderRegistryImpl.BLOCK;

	/**
	 * Register a color provider for one or more objects.
	 *
	 * @param provider The color provider to register.
	 * @param objects  The objects which should be colored using this provider.
	 */
	@SuppressWarnings("unchecked")
	void register(P provider, T... objects);

	/**
	 * Get a color provider for the given object.
	 *
	 * <p>Please note that the underlying registry may not be fully populated or stable until the game has started,
	 * as other mods may overwrite the registry.
	 *
	 * @param object The object to acquire the provider for.
	 * @return The registered mapper for this provider, or {@code null} if none is registered or available.
	 */
	@Nullable
	P get(T object);
}

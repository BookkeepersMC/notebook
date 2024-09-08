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

import java.util.Set;

import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.client.color.biome.BiomeColorProvider;

import com.bookkeepersmc.notebook.impl.client.rendering.ColorResolverRegistryImpl;

public final class ColorResolverRegistry {
	private ColorResolverRegistry() {
	}

	/**
	 * Registers a custom {@link BiomeColorProvider} for use in {@link net.minecraft.world.BlockRenderView#getColor}. This method should be
	 * called during client initialization.
	 *
	 * @param resolver the resolver to register
	 */
	public static void register(BiomeColorProvider resolver) {
		ColorResolverRegistryImpl.register(resolver);
	}

	/**
	 * Gets a view of all registered {@link BiomeColorProvider}s, including all vanilla resolvers.
	 *
	 * @return a view of all registered resolvers
	 */
	@UnmodifiableView
	public static Set<BiomeColorProvider> getAllResolvers() {
		return ColorResolverRegistryImpl.getAllResolvers();
	}

	/**
	 * Gets a view of all registered {@link BiomeColorProvider}s, not including vanilla resolvers.
	 *
	 * @return a view of all registered custom resolvers
	 */
	@UnmodifiableView
	public static Set<BiomeColorProvider> getCustomResolvers() {
		return ColorResolverRegistryImpl.getCustomResolvers();
	}

	/**
	 * Checks whether the given {@link BiomeColorProvider} is registered. Vanilla resolvers are always registered.
	 *
	 * @param resolver the resolver
	 * @return whether the given resolver is registered
	 */
	public static boolean isRegistered(BiomeColorProvider resolver) {
		return getAllResolvers().contains(resolver);
	}
}

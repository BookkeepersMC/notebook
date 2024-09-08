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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.client.color.biome.BiomeColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.BiomeColorCache;

public final class ColorResolverRegistryImpl {
	// Includes vanilla resolvers
	private static final Set<BiomeColorProvider> ALL_RESOLVERS = new HashSet<>();
	// Does not include vanilla resolvers
	private static final Set<BiomeColorProvider> CUSTOM_RESOLVERS = new HashSet<>();
	private static final Set<BiomeColorProvider> ALL_RESOLVERS_VIEW = Collections.unmodifiableSet(ALL_RESOLVERS);
	private static final Set<BiomeColorProvider> CUSTOM_RESOLVERS_VIEW = Collections.unmodifiableSet(CUSTOM_RESOLVERS);

	static {
		ALL_RESOLVERS.add(BiomeColors.GRASS_COLOR);
		ALL_RESOLVERS.add(BiomeColors.FOLIAGE_COLOR);
		ALL_RESOLVERS.add(BiomeColors.WATER_COLOR);
	}

	private ColorResolverRegistryImpl() {
	}

	public static void register(BiomeColorProvider resolver) {
		ALL_RESOLVERS.add(resolver);
		CUSTOM_RESOLVERS.add(resolver);
	}

	@UnmodifiableView
	public static Set<BiomeColorProvider> getAllResolvers() {
		return ALL_RESOLVERS_VIEW;
	}

	@UnmodifiableView
	public static Set<BiomeColorProvider> getCustomResolvers() {
		return CUSTOM_RESOLVERS_VIEW;
	}

	public static Reference2ReferenceMap<BiomeColorProvider, BiomeColorCache> createCustomCacheMap(Function<BiomeColorProvider, BiomeColorCache> cacheFactory) {
		Reference2ReferenceOpenHashMap<BiomeColorProvider, BiomeColorCache> map = new Reference2ReferenceOpenHashMap<>();

		for (BiomeColorProvider resolver : CUSTOM_RESOLVERS) {
			map.put(resolver, cacheFactory.apply(resolver));
		}

		map.trim();
		return map;
	}
}

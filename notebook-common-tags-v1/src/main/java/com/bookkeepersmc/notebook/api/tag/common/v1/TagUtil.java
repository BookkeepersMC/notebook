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
package com.bookkeepersmc.notebook.api.tag.common.v1;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.ResourceKey;
import net.minecraft.registry.tag.TagKey;


/**
 * A Helper class for checking whether a {@link TagKey} contains some entry.
 * This can be useful for {@link TagKey}s whose type has no easy way of querying if they are in a tag, such as {@link net.minecraft.world.item.enchantment.Enchantment}s.
 *
 * <p>For dynamic registry entries, use {@link #isIn(RegistryAccess, TagKey, Object)} with a non-null dynamic registry manager.
 * For non-dynamic registry entries, the simpler {@link #isIn(TagKey, Object)} can be used.
 */
public final class TagUtil {
	public static final String C_TAG_NAMESPACE = "c";
	public static final String NOTEBOOK_TAG_NAMESPACE = "notebook";

	private TagUtil() {
	}

	/**
	 * See {@link TagUtil#isIn(RegistryAccess, TagKey, Object)} to check tags that refer to entries in dynamic
	 * registries, such as {@link net.minecraft.world.level.biome.Biome}s.
	 * @return if the entry is in the provided tag.
	 */
	public static <T> boolean isIn(TagKey<T> tagKey, T entry) {
		return isIn(null, tagKey, entry);
	}

	/**
	 * @param registryManager the registry manager instance of the client or server. If the tag refers to entries
	 *                        within a dynamic registry, such as {@link net.minecraft.world.level.biome.Biome}s,
	 *                        this must be passed to correctly evaluate the tag. Otherwise, the registry is found by
	 *                        looking in {@link BuiltInRegistries#REGISTRY}.
	 * @return if the entry is in the provided tag.
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean isIn(@Nullable DynamicRegistryManager registryManager, TagKey<T> tagKey, T entry) {
		Optional<? extends Registry<?>> maybeRegistry;
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(entry);

		if (registryManager != null) {
			maybeRegistry = registryManager.getOptional(tagKey.registry());
		} else {
			maybeRegistry = BuiltInRegistries.ROOT.getOrEmpty(tagKey.registry().getValue());
		}

		if (maybeRegistry.isPresent()) {
			if (tagKey.isOfRegistry(maybeRegistry.get().getKey())) {
				Registry<T> registry = (Registry<T>) maybeRegistry.get();

				Optional<ResourceKey<T>> maybeKey = registry.getKey(entry);

				// Check synced tag
				if (maybeKey.isPresent()) {
					return registry.getHolderOrThrow(maybeKey.get()).isIn(tagKey);
				}
			}
		}

		return false;
	}
}

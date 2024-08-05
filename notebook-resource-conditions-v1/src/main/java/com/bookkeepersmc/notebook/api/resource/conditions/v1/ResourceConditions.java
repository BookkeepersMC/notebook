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
package com.bookkeepersmc.notebook.api.resource.conditions.v1;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlag;

import com.bookkeepersmc.notebook.impl.resource.conditions.conditions.*;

public final class ResourceConditions {
	private static final Map<ResourceLocation, ResourceConditionType<?>> REGISTERED_CONDITIONS = new ConcurrentHashMap<>();

	public static final String CONDITIONS_KEY = "notebook:load_conditions";

	public static final String OVERLAYS_KEY = "notebook:overlays";

	private ResourceConditions() {
	}

	public static void register(ResourceConditionType<?> conditionType) {
		Objects.requireNonNull(conditionType, "Condition must not be null!");

		if (REGISTERED_CONDITIONS.put(conditionType.id(), conditionType) != null) {
			throw new IllegalArgumentException("Duplicate resource condition registered with id " + conditionType.id());
		}
	}

	public static ResourceConditionType<?> getConditionType(ResourceLocation id) {
		return REGISTERED_CONDITIONS.get(id);
	}

	public static ResourceCondition alwaysTrue() {
		return new TrueResourceCondition();
	}

	public static ResourceCondition not(ResourceCondition condition) {
		return new NotResourceCondition(condition);
	}

	public static ResourceCondition and(ResourceCondition... conditions) {
		return new AndResourceCondition(List.of(conditions));
	}

	public static ResourceCondition or(ResourceCondition... conditions) {
		return new OrResourceCondition(List.of(conditions));
	}

	public static ResourceCondition allModsLoaded(String... modIds) {
		return new AllModsLoadedResourceCondition(List.of(modIds));
	}

	public static ResourceCondition anyModsLoaded(String... modIds) {
		return new AnyModsLoadedResourceCondition(List.of(modIds));
	}

	@SafeVarargs
	public static <T> ResourceCondition tagsPopulated(TagKey<T>... tags) {
		return new TagsPopulatedResourceCondition(tags);
	}

	@SafeVarargs
	public static <T> ResourceCondition tagsPopulated(ResourceKey<? extends Registry<T>> registry, TagKey<T>... tags) {
		return new TagsPopulatedResourceCondition(registry.location(), tags);
	}

	public static ResourceCondition featuresEnabled(ResourceLocation... features) {
		return new FeaturesEnabledResourceCondition(features);
	}

	public static ResourceCondition featuresEnabled(FeatureFlag... features) {
		return new FeaturesEnabledResourceCondition(features);
	}

	@SafeVarargs
	public static <T> ResourceCondition registryContains(ResourceKey<T>... entries) {
		return new RegistryContainsResourceCondition(entries);
	}

	public static <T> ResourceCondition registryContains(ResourceKey<? extends Registry<T>> registry, ResourceLocation... entries) {
		return new RegistryContainsResourceCondition(registry.location(), entries);
	}
}

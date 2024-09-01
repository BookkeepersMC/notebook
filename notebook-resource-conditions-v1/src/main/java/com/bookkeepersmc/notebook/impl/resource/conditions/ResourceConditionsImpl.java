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
package com.bookkeepersmc.notebook.impl.resource.conditions;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ResourceKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.api.ModInitializer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceConditions;

public final class ResourceConditionsImpl implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Notebook Resource Conditions");
	public static FeatureFlagBitSet currentFeatures = null;

	@Override
	public void onInitialize() {
		ResourceConditions.register(DefaultResourceConditionTypes.TRUE);
		ResourceConditions.register(DefaultResourceConditionTypes.NOT);
		ResourceConditions.register(DefaultResourceConditionTypes.AND);
		ResourceConditions.register(DefaultResourceConditionTypes.OR);
		ResourceConditions.register(DefaultResourceConditionTypes.ALL_MODS_LOADED);
		ResourceConditions.register(DefaultResourceConditionTypes.ANY_MODS_LOADED);
		ResourceConditions.register(DefaultResourceConditionTypes.TAGS_POPULATED);
		ResourceConditions.register(DefaultResourceConditionTypes.FEATURES_ENABLED);
		ResourceConditions.register(DefaultResourceConditionTypes.REGISTRY_CONTAINS);
	}

	public static boolean applyResourceConditions(JsonObject obj, String dataType, Identifier key, @Nullable RegistryOps.RegistryInfoLookup registryLookup) {
		boolean debugLogEnabled = ResourceConditionsImpl.LOGGER.isDebugEnabled();

		if (obj.has(ResourceConditions.CONDITIONS_KEY)) {
			DataResult<ResourceCondition> conditions = ResourceCondition.CONDITION_CODEC.parse(JsonOps.INSTANCE, obj.get(ResourceConditions.CONDITIONS_KEY));

			if (conditions.isSuccess()) {
				boolean matched = conditions.getOrThrow().test(registryLookup);

				if (debugLogEnabled) {
					String verdict = matched ? "Allowed" : "Rejected";
					ResourceConditionsImpl.LOGGER.debug("{} resource of type {} with id {}", verdict, dataType, key);
				}

				return matched;
			} else {
				ResourceConditionsImpl.LOGGER.error("Failed to parse resource conditions for file of type {} with id {}, skipping: {}", dataType, key, conditions.error().get().message());
			}
		}

		return true;
	}

	public static boolean conditionsMet(List<ResourceCondition> conditions, @Nullable RegistryOps.RegistryInfoLookup registryLookup, boolean and) {
		for (ResourceCondition condition : conditions) {
			if (condition.test(registryLookup) != and) {
				return !and;
			}
		}

		return and;
	}

	public static boolean modsLoaded(List<String> modIds, boolean and) {
		for (String modId : modIds) {
			if (NotebookLoader.getInstance().isModLoaded(modId) != and) {
				return !and;
			}
		}

		return and;
	}

	public static final ThreadLocal<Map<ResourceKey<?>, Set<Identifier>>> LOADED_TAGS = new ThreadLocal<>();

	public static void setTags(List<Registry.TagPending<?>> tags) {
		Map<ResourceKey<?>, Set<Identifier>> tagMap = new IdentityHashMap<>();

		for (Registry.TagPending<?> registryTags : tags) {
			tagMap.put(registryTags.getKey(), registryTags.asLookup().streamTagKeys().map(TagKey::id).collect(Collectors.toSet()));
		}

		LOADED_TAGS.set(tagMap);
	}

	// Cannot use registry because tags are not loaded to the registry at this stage yet.
	public static boolean tagsPopulated(Identifier registryId, List<Identifier> tags) {
		Map<ResourceKey<?>, Set<Identifier>> tagMap = LOADED_TAGS.get();

		if (tagMap == null) {
			LOGGER.warn("Can't retrieve registry {}, failing tags_populated resource condition check", registryId);
			return false;
		}

		Set<Identifier> tagSet = tagMap.get(ResourceKey.ofRegistry(registryId));

		if (tagSet == null) {
			return tags.isEmpty();
		} else {
			return tagSet.containsAll(tags);
		}
	}

	public static boolean featuresEnabled(Collection<Identifier> features) {
		MutableBoolean foundUnknown = new MutableBoolean();
		FeatureFlagBitSet set = FeatureFlags.MAIN_REGISTRY.checkedBitSetOf(features, (id) -> {
			LOGGER.info("Found unknown feature {}, treating it as failure", id);
			foundUnknown.setTrue();
		});

		if (foundUnknown.booleanValue()) {
			return false;
		}

		if (currentFeatures == null) {
			LOGGER.warn("Can't retrieve current features, failing features_enabled resource condition check.");
			return false;
		}

		return set.isIn(currentFeatures);
	}

	public static boolean registryContains(@Nullable RegistryOps.RegistryInfoLookup registryLookup, Identifier registryId, List<Identifier> entries) {
		ResourceKey<? extends Registry<Object>> registryKey = ResourceKey.ofRegistry(registryId);

		if (registryLookup == null) {
			LOGGER.warn("Can't retrieve registry {}, failing registry_contains resource condition check", registryId);
			return false;
		}

		Optional<RegistryOps.RegistryInfo<Object>> wrapper = registryLookup.lookup(registryKey);

		if (wrapper.isPresent()) {
			for (Identifier id : entries) {
				if (wrapper.get().getter().getHolder(ResourceKey.of(registryKey, id)).isEmpty()) {
					return false;
				}
			}

			return true;
		} else {
			return entries.isEmpty();
		}
	}
}

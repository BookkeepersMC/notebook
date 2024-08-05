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

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceConditionType;
import com.bookkeepersmc.notebook.impl.resource.conditions.conditions.*;

public class DefaultResourceConditionTypes {
	public static final ResourceConditionType<TrueResourceCondition> TRUE = createResourceConditionType("true", TrueResourceCondition.CODEC);
	public static final ResourceConditionType<NotResourceCondition> NOT = createResourceConditionType("not", NotResourceCondition.CODEC);
	public static final ResourceConditionType<OrResourceCondition> OR = createResourceConditionType("or", OrResourceCondition.CODEC);
	public static final ResourceConditionType<AndResourceCondition> AND = createResourceConditionType("and", AndResourceCondition.CODEC);
	public static final ResourceConditionType<AllModsLoadedResourceCondition> ALL_MODS_LOADED = createResourceConditionType("all_mods_loaded", AllModsLoadedResourceCondition.CODEC);
	public static final ResourceConditionType<AnyModsLoadedResourceCondition> ANY_MODS_LOADED = createResourceConditionType("any_mods_loaded", AnyModsLoadedResourceCondition.CODEC);
	public static final ResourceConditionType<TagsPopulatedResourceCondition> TAGS_POPULATED = createResourceConditionType("tags_populated", TagsPopulatedResourceCondition.CODEC);
	public static final ResourceConditionType<FeaturesEnabledResourceCondition> FEATURES_ENABLED = createResourceConditionType("features_enabled", FeaturesEnabledResourceCondition.CODEC);
	public static final ResourceConditionType<RegistryContainsResourceCondition> REGISTRY_CONTAINS = createResourceConditionType("registry_contains", RegistryContainsResourceCondition.CODEC);

	private static <T extends ResourceCondition> ResourceConditionType<T> createResourceConditionType(String name, MapCodec<T> codec) {
		return ResourceConditionType.create(ResourceLocation.fromNamespaceAndPath("notebook", name), codec);
	}
}

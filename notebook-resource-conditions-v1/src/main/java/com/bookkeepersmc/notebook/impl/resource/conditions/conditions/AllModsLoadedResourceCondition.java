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
package com.bookkeepersmc.notebook.impl.resource.conditions.conditions;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryOps;

import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceConditionType;
import com.bookkeepersmc.notebook.impl.resource.conditions.DefaultResourceConditionTypes;
import com.bookkeepersmc.notebook.impl.resource.conditions.ResourceConditionsImpl;

public record AllModsLoadedResourceCondition(List<String> modIds) implements ResourceCondition {
	public static final MapCodec<AllModsLoadedResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.listOf().fieldOf("values").forGetter(AllModsLoadedResourceCondition::modIds)
	).apply(instance, AllModsLoadedResourceCondition::new));

	@Override
	public ResourceConditionType<?> getType() {
		return DefaultResourceConditionTypes.ALL_MODS_LOADED;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryLookup) {
		return ResourceConditionsImpl.modsLoaded(this.modIds(), true);
	}
}

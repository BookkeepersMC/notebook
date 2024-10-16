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
package com.bookkeepersmc.notebook.impl.tag.common.v1;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.ResourceKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.StructureFeature;

import com.bookkeepersmc.notebook.api.tag.common.v1.TagUtil;

public record TagRegistration<T>(ResourceKey<Registry<T>> resourceKey) {
	public static final TagRegistration<Item> ITEM_TAG = new TagRegistration<>(Registries.ITEM);
	public static final TagRegistration<Block> BLOCK_TAG = new TagRegistration<>(Registries.BLOCK);
	public static final TagRegistration<Biome> BIOME_TAG = new TagRegistration<>(Registries.BIOME);
	public static final TagRegistration<StructureFeature> STRUCTURE_TAG = new TagRegistration<>(Registries.STRUCTURE_FEATURE);
	public static final TagRegistration<Fluid> FLUID_TAG = new TagRegistration<>(Registries.FLUID);
	public static final TagRegistration<EntityType<?>> ENTITY_TYPE_TAG = new TagRegistration<>(Registries.ENTITY_TYPE);
	public static final TagRegistration<Enchantment> ENCHANTMENT_TAG = new TagRegistration<>(Registries.ENCHANTMENT);

	public TagKey<T> registerNotebook(String tagId) {
		return TagKey.of(resourceKey, Identifier.of(TagUtil.NOTEBOOK_TAG_NAMESPACE, tagId));
	}

	public TagKey<T> registerC(String tagId) {
		return TagKey.of(resourceKey, Identifier.of(TagUtil.C_TAG_NAMESPACE, tagId));
	}
}

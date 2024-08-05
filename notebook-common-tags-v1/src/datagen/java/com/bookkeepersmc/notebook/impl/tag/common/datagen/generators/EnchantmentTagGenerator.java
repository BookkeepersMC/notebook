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
package com.bookkeepersmc.notebook.impl.tag.common.datagen.generators;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.TagDataProvider;
import com.bookkeepersmc.notebook.api.tag.common.v1.ConventionalEnchantmentTags;

public final class EnchantmentTagGenerator extends TagDataProvider.EnchantmentTagProvider {
	public EnchantmentTagGenerator(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(ConventionalEnchantmentTags.INCREASE_BLOCK_DROPS)
				.add(Enchantments.FORTUNE);
		tag(ConventionalEnchantmentTags.INCREASE_ENTITY_DROPS)
				.add(Enchantments.LOOTING);
		tag(ConventionalEnchantmentTags.WEAPON_DAMAGE_ENHANCEMENTS)
				.add(Enchantments.SHARPNESS)
				.add(Enchantments.SMITE)
				.add(Enchantments.BANE_OF_ARTHROPODS)
				.add(Enchantments.POWER)
				.add(Enchantments.IMPALING);
		tag(ConventionalEnchantmentTags.ENTITY_SPEED_ENHANCEMENTS)
				.add(Enchantments.SOUL_SPEED)
				.add(Enchantments.SWIFT_SNEAK)
				.add(Enchantments.DEPTH_STRIDER);
		tag(ConventionalEnchantmentTags.ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS)
				.add(Enchantments.FEATHER_FALLING)
				.add(Enchantments.FROST_WALKER);
		tag(ConventionalEnchantmentTags.ENTITY_DEFENSE_ENHANCEMENTS)
				.add(Enchantments.PROTECTION)
				.add(Enchantments.BLAST_PROTECTION)
				.add(Enchantments.PROJECTILE_PROTECTION)
				.add(Enchantments.FIRE_PROTECTION)
				.add(Enchantments.RESPIRATION)
				.add(Enchantments.FEATHER_FALLING);

		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22
		tag(ConventionalEnchantmentTags.ENTITY_SPEED_ENHANCEMENTS)
				.addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "entity_movement_enhancement"));
		tag(ConventionalEnchantmentTags.ENTITY_DEFENSE_ENHANCEMENTS)
				.addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "entity_defense_enhancement"));
	}
}

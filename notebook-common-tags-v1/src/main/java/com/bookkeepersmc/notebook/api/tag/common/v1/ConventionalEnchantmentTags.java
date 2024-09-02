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

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.tag.TagKey;

import com.bookkeepersmc.notebook.impl.tag.common.v1.TagRegistration;

public final class ConventionalEnchantmentTags {
	private ConventionalEnchantmentTags() {
	}

	/**
	 * A tag containing enchantments that increase the amount or
	 * quality of drops from blocks, such as {@link net.minecraft.world.item.enchantment.Enchantments#FORTUNE}.
	 */
	public static final TagKey<Enchantment> INCREASE_BLOCK_DROPS = register("increase_block_drops");
	/**
	 * A tag containing enchantments that increase the amount or
	 * quality of drops from entities, such as {@link net.minecraft.world.item.enchantment.Enchantments#LOOTING}.
	 */
	public static final TagKey<Enchantment> INCREASE_ENTITY_DROPS = register("increase_entity_drops");
	/**
	 * For enchantments that increase the damage dealt by an item.
	 */
	public static final TagKey<Enchantment> WEAPON_DAMAGE_ENHANCEMENTS = register("weapon_damage_enhancements");
	/**
	 * For enchantments that increase movement speed for entity wearing armor enchanted with it.
	 */
	public static final TagKey<Enchantment> ENTITY_SPEED_ENHANCEMENTS = register("entity_speed_enhancements");
	/**
	 * For enchantments that applies movement-based benefits unrelated to speed for the entity wearing armor enchanted with it.
	 * Example: Reducing falling speeds ({@link net.minecraft.world.item.enchantment.Enchantments#FEATHER_FALLING}) or allowing walking on water ({@link net.minecraft.world.item.enchantment.Enchantments#FROST_WALKER})
	 */
	public static final TagKey<Enchantment> ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS = register("entity_auxiliary_movement_enhancements");
	/**
	 * For enchantments that decrease damage taken or otherwise benefit, in regard to damage, the entity wearing armor enchanted with it.
	 */
	public static final TagKey<Enchantment> ENTITY_DEFENSE_ENHANCEMENTS = register("entity_defense_enhancements");

	private static TagKey<Enchantment> register(String tagId) {
		return TagRegistration.ENCHANTMENT_TAG.registerC(tagId);
	}
}

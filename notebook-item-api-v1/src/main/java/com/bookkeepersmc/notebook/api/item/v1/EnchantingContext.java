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
package com.bookkeepersmc.notebook.api.item.v1;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

/**
 * An enum that describes the contexts in which the game checks whether an enchantment can be applied to an item.
 */
public enum EnchantingContext {
	/**
	 * When checking if an item is <em>acceptable</em> for a given enchantment, i.e if the item should be able to bear
	 * that enchantment. This includes anvils, the {@code enchant_randomly} loot function, and the {@code /enchant} command.
	 *
	 * @see Enchantment#isAcceptableItem(ItemStack)
	 */
	ACCEPTABLE,
	/**
	 * When checking for an enchantment's <em>primary</em> items. This includes enchanting in an enchanting table, random
	 * mob equipment, and the {@code enchant_with_levels} loot function.
	 *
	 * @see Enchantment#isPrimaryItem(ItemStack)
	 */
	PRIMARY
}

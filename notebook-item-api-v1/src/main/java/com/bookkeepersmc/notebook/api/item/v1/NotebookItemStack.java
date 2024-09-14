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
import net.minecraft.registry.Holder;

import com.bookkeepersmc.notebook.api.util.TriState;

/**
 * Notebook-provided extensions for {@link ItemStack}.
 * This interface is automatically implemented on all item stacks via Mixin and interface injection.
 */
public interface NotebookItemStack {
	/**
	 * Return a leftover item for use in recipes.
	 *
	 * <p>See {@link ItemExtensions#getRecipeRemainder(ItemStack)} for a more in depth description.
	 *
	 * <p>Stack-aware version of {@link net.minecraft.item.Item#getRecipeRemainder()}.
	 *
	 * @return the leftover item
	 */
	default ItemStack getRecipeRemainder() {
		return ((ItemStack) this).getItem().getRecipeRemainder((ItemStack) this);
	}

	/**
	 * Determines whether this {@link ItemStack} can be enchanted with the given {@link Enchantment}.
	 *
	 * <p>When checking whether an enchantment can be applied to an {@link ItemStack}, use this method instead of
	 * {@link Enchantment#isAcceptableItem(ItemStack)} or {@link Enchantment#isPrimaryItem(ItemStack)}, with the appropriate
	 * {@link EnchantingContext}.</p>
	 *
	 * @param enchantment the enchantment to check
	 * @param context the context in which the enchantment is being checked
	 * @return whether the enchantment is allowed to apply to the stack
	 * @see ItemExtensions#canBeEnchantedWith(ItemStack, Holder, EnchantingContext)
	 */
	default boolean canBeEnchantedWith(Holder<Enchantment> enchantment, EnchantingContext context) {
		TriState result = EnchantmentEvents.ALLOW_ENCHANTING.invoker().allowEnchanting(
				enchantment,
				(ItemStack) this,
				context
		);
		return result.orElseGet(() -> ((ItemStack) this).getItem().canBeEnchantedWith((ItemStack) this, enchantment, context));
	}
}

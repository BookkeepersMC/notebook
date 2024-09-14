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

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;
import com.bookkeepersmc.notebook.api.util.TriState;

public final class EnchantmentEvents {
	private EnchantmentEvents() { }

	/**
	 * An event that allows overriding whether an {@link Enchantment} can be applied to an {@link ItemStack}.
	 *
	 * <p>This should only be used to modify the behavior of <em>external</em> items with regards to <em>external</em> enchantments,
	 * where 'external' means either vanilla or from another mod. For instance, a mod might allow enchanting a pickaxe
	 * with Sharpness (and only Sharpness) under certain specific conditions.</p>
	 *
	 * <p>To modify the behavior of your own modded <em>enchantments</em>, specify a custom tag for {@link Enchantment.Properties#supportedItems()} instead.
	 * To modify the behavior of your own modded <em>items</em>, add to the applicable tags instead, when that suffices.
	 * Note that this event triggers <em>before</em> {@link ItemExtensions#canBeEnchantedWith(ItemStack, Holder, EnchantingContext)},
	 * and that method will only be called if no listeners override it.</p>
	 *
	 * <p>Note that allowing an enchantment using this event does not guarantee the item will receive that enchantment,
	 * only that it isn't forbidden from doing so.</p>
	 *
	 * @see AllowEnchanting#allowEnchanting(Holder, ItemStack, EnchantingContext)
	 * @see Enchantment#isAcceptableItem(ItemStack)
	 * @see ItemExtensions#canBeEnchantedWith(ItemStack, Holder, EnchantingContext)
	 */
	public static final Event<AllowEnchanting> ALLOW_ENCHANTING = EventFactory.createArrayBacked(
			AllowEnchanting.class,
			callbacks -> (enchantment, target, context) -> {
				for (AllowEnchanting callback : callbacks) {
					TriState result = callback.allowEnchanting(enchantment, target, context);

					if (result != TriState.DEFAULT) {
						return result;
					}
				}

				return TriState.DEFAULT;
			}
	);

	@FunctionalInterface
	public interface AllowEnchanting {
		/**
		 * Checks whether an {@link Enchantment} should be applied to a given {@link ItemStack}.
		 *
		 * @param enchantment the enchantment that may be applied
		 * @param target the target item
		 * @param enchantingContext the enchanting context in which this check is made
		 * @return {@link TriState#TRUE} if the enchantment may be applied, {@link TriState#FALSE} if it
		 * may not, {@link TriState#DEFAULT} to fall back to other callbacks/vanilla behavior
		 * @see EnchantingContext
		 */
		TriState allowEnchanting(
				Holder<Enchantment> enchantment,
				ItemStack target,
				EnchantingContext enchantingContext
		);
	}
}

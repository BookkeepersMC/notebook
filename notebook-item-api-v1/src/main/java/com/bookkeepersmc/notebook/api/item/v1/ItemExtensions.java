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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Holder;
import net.minecraft.util.Hand;

import com.bookkeepersmc.notebook.impl.item.NotebookItemInternals;

public interface ItemExtensions {
	/**
	 * When the components of an item stack in the main hand or off hand changes, vanilla runs an "update animation".
	 * This function is called on the client side when the components or count of the stack has changed, but not the item,
	 * and returning false cancels this animation.
	 *
	 * @param player   the current player; this may be safely cast to {@link net.minecraft.client.network.ClientPlayerEntity} in client-only code
	 * @param hand     the hand; this function applies both to the main hand and the off hand
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to run the vanilla animation, false to cancel it.
	 */
	default boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
		return true;
	}

	/**
	 * When the components of the selected stack changes, block breaking progress is reset.
	 * This function is called when the components of the selected stack has changed,
	 * and returning true allows the block breaking progress to continue.
	 *
	 * @param player   the player breaking the block
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to allow continuing block breaking, false to reset the progress.
	 */
	default boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
		return false;
	}

	/**
	 * Returns a leftover item stack after {@code stack} is consumed in a recipe.
	 * (This is also known as "recipe remainder".)
	 * For example, using a lava bucket in a furnace as fuel will leave an empty bucket.
	 *
	 * <p>Here is an example for a recipe remainder that increments the item's damage.
	 *
	 * <pre>{@code
	 *  if (stack.getDamage() < stack.getMaxDamage() - 1) {
	 *  	ItemStack moreDamaged = stack.copy();
	 *  	moreDamaged.setDamage(stack.getDamage() + 1);
	 *  	return moreDamaged;
	 *  }
	 *
	 *  return ItemStack.EMPTY;
	 * }</pre>
	 *
	 *
	 * <p>This is a stack-aware version of {@link Item#getRecipeRemainder()}.
	 *
	 * <p>Note that simple item remainders can also be set via {@link Item.Settings#recipeRemainder(Item)}.
	 *
	 * <p>If you want to get a remainder for a stack,
	 * is recommended to use the stack version of this method: {@link NotebookItemStack#getRecipeRemainder()}.
	 *
	 * @param stack the consumed {@link ItemStack}
	 * @return the leftover item stack
	 */
	default ItemStack getRecipeRemainder(ItemStack stack) {
		return ((Item) this).hasRecipeRemainder() ? ((Item) this).getRecipeRemainder().getDefaultStack() : ItemStack.EMPTY;
	}

	/**
	 * Determines if the item is allowed to receive an {@link Enchantment}. This can be used to manually override what
	 * enchantments a modded item is able to receive.
	 *
	 * <p>For example, one might want a modded item to be able to receive Unbreaking, but not Mending, which cannot be
	 * achieved with the vanilla tag system alone. Alternatively, one might want to do the same thing with enchantments
	 * from other mods, which don't have a similar tag system in general.</p>
	 *
	 * <p>Note that this method is only called <em>after</em> the {@link EnchantmentEvents#ALLOW_ENCHANTING} event, and
	 * only if none of the listeners to that event override the result.</p>
	 *
	 * @param stack the current stack
	 * @param enchantment the enchantment to check
	 * @param context the context in which the enchantment is being checked
	 * @return whether the enchantment is allowed to apply to the stack
	 */
	default boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		return context == EnchantingContext.PRIMARY
				? enchantment.getValue().isPrimaryItem(stack)
				: enchantment.getValue().isAcceptableItem(stack);
	}

	/**
	 * Notebook-provided extensions for {@link Item.Settings}.
	 * This interface is automatically implemented on all item settings via Mixin and interface injection.
	 */
	interface Settings {
		/**
		 * Sets the equipment slot provider of the item.
		 *
		 * @param equipmentSlotProvider the equipment slot provider
		 * @return this builder
		 */
		default Item.Settings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
			NotebookItemInternals.computeExtraData((Item.Settings) this).equipmentSlot(equipmentSlotProvider);
			return (Item.Settings) this;
		}

		/**
		 * Sets the custom damage handler of the item.
		 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageable()} returns true.
		 *
		 * @see CustomDamageHandler
		 */
		default Item.Settings customDamage(CustomDamageHandler handler) {
			NotebookItemInternals.computeExtraData((Item.Settings) this).customDamage(handler);
			return (Item.Settings) this;
		}
	}
}

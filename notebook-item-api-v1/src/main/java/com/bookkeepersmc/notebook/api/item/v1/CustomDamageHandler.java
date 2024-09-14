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

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Allows an item to run custom logic when {@link net.minecraft.item.ItemStack#damageEquipment(int, LivingEntity, EquipmentSlot)} is called.
 * This is useful for items that, for example, may drain durability from some other source before damaging
 * the stack itself.
 *
 * <p>Custom damage handlers can be set with {@link ItemExtensions.Settings#customDamage}.
 */
@FunctionalInterface
public interface CustomDamageHandler {
	/**
	 * Called to apply damage to the given stack.
	 * This can be used to e.g. drain from a battery before actually damaging the item.
	 * Note that this does not get called if non-entities, such as dispensers, are damaging the item,
	 * or for thrown tridents.
	 * Calling {@code breakCallback} breaks the item, bypassing the vanilla logic. The return value is
	 * ignored in this case.
	 * @param amount the amount of damage originally requested
	 * @return The amount of damage to pass to vanilla's logic
	 */
	int damage(ItemStack stack, int amount, LivingEntity livingEntity, EquipmentSlot slot, Runnable breakCallback);
}

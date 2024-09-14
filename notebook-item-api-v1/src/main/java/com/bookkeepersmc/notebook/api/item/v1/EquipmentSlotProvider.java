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
 * A provider for the preferred equipment slot of an item.
 * This can be used to give non-armor items, such as blocks,
 * an armor slot that they can go in.
 *
 * <p>The preferred equipment slot of an item stack can be queried using
 * {@link LivingEntity#getPreferredEquipmentSlot(ItemStack) LivingEntity.getPreferredEquipmentSlot()}.
 *
 * <p>Equipment slot providers can be set with {@link ItemExtensions.Settings#equipmentSlot(EquipmentSlotProvider)}.
 *
 * <p>Note that items extending {@link net.minecraft.item.ArmorItem} don't need to use this
 * as there's TODO().
 */
@FunctionalInterface
public interface EquipmentSlotProvider {
	/**
	 * Gets the preferred equipment slot for an item stack.
	 *
	 * <p>If there is no preferred armor equipment slot for the stack,
	 * {@link EquipmentSlot#MAINHAND} can be returned.
	 *
	 * <p>Callers are expected to check themselves whether the slot is available for the
	 * {@code entity} using {@link LivingEntity#canUseSlot}. For example, players
	 * cannot use {@link EquipmentSlot#BODY}, which is instead used for items like horse armors.
	 *
	 * @param entity the entity
	 * @param stack the item stack
	 * @return the preferred equipment slot
	 */
	EquipmentSlot getPreferredEquipmentSlot(LivingEntity entity, ItemStack stack);
}

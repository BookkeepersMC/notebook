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
package com.bookkeepersmc.notebook.mixin.item;

import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import com.bookkeepersmc.notebook.api.item.v1.CustomDamageHandler;
import com.bookkeepersmc.notebook.api.item.v1.NotebookItemStack;
import com.bookkeepersmc.notebook.impl.item.NotebookItemExtensions;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements NotebookItemStack {
	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract void decrement(int amount);

	@WrapOperation(
			method = "damageEquipment(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;damageEquipment(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V"
			)
	)
	private void hookDamage(ItemStack instance, int damage, ServerWorld world, ServerPlayerEntity player, Consumer<Item> consumer, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) EquipmentSlot slot) {
		CustomDamageHandler handler = ((NotebookItemExtensions) getItem()).notebook_getCustomDamageHandler();
		/*
			This is called by creative mode players, post-24w21a.
			The other damage method (which original.call discards) handles the creative mode check.
			Since it doesn't make sense to call an event to calculate a to-be-discarded value
			(and to prevent mods from breaking item stacks in Creative mode),
			we preserve the pre-24w21a behavior of not calling in creative mode.
		*/

		if (handler != null && !entity.isInCreativeMode()) {
			MutableBoolean mut = new MutableBoolean(false);
			damage = handler.damage((ItemStack) (Object) this, damage, entity, slot, () -> {
				mut.setTrue();
				this.decrement(1);
				consumer.accept(this.getItem());
			});

			if (mut.booleanValue()) return;
		}

		original.call(instance, damage, world, player, consumer);
	}
}

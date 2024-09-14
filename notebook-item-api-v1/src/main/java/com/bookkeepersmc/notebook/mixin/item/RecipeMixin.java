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

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInput;
import net.minecraft.util.collection.DefaultedList;

import com.bookkeepersmc.notebook.impl.item.RecipeRemainderHandler;

@Mixin(Recipe.class)
public interface RecipeMixin<T extends RecipeInput> {
	@Inject(method = "getRemainder", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeInput;get(I)Lnet/minecraft/item/ItemStack;"))
	default void captureStack(T inventory, CallbackInfoReturnable<DefaultedList<ItemStack>> cir, @Local int i) {
		RecipeRemainderHandler.REMAINDER_STACK.set(inventory.get(i).getRecipeRemainder());
	}

	@Redirect(method = "getRemainder", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;hasRecipeRemainder()Z"))
	private boolean hasStackRemainder(Item instance) {
		return !RecipeRemainderHandler.REMAINDER_STACK.get().isEmpty();
	}

	@Redirect(method = "getRemainder", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getRecipeRemainder()Lnet/minecraft/item/Item;"))
	private Item replaceGetRecipeRemainder(Item instance) {
		return Items.AIR;
	}

	@Redirect(method = "getRemainder", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
	private Object getStackRemainder(DefaultedList<ItemStack> inventory, int index, Object element) {
		Object remainder = inventory.set(index, RecipeRemainderHandler.REMAINDER_STACK.get());
		RecipeRemainderHandler.REMAINDER_STACK.remove();
		return remainder;
	}
}

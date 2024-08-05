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
package com.bookkeepersmc.notebook.api.recipe.v1;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.bookkeepersmc.notebook.impl.recipe.builtin.*;

public final class DefaultCustomIngredients {
	public static Ingredient all(Ingredient... ingredients) {
		for (Ingredient ing : ingredients) Objects.requireNonNull(ing, "Ingredient cannot be null");

		return new AllIngredient(List.of(ingredients)).toVanilla();
	}

	public static Ingredient any(Ingredient... ingredients) {
		for (Ingredient ing : ingredients) Objects.requireNonNull(ing, "Ingredient cannot be null");

		return new AnyIngredient(List.of(ingredients)).toVanilla();
	}

	public static Ingredient difference(Ingredient base, Ingredient subtracted) {
		Objects.requireNonNull(base, "Base ingredient cannot be null");
		Objects.requireNonNull(subtracted, "Subtracted ingredient cannot be null");

		return new DifferenceIngredient(base, subtracted).toVanilla();
	}

	public static Ingredient components(Ingredient base, DataComponentPatch components) {
		Objects.requireNonNull(base, "Base ingredient cannot be null");
		Objects.requireNonNull(components, "Component changes cannot be null");

		return new ComponentsIngredient(base, components).toVanilla();
	}

	public static Ingredient components(Ingredient base, UnaryOperator<DataComponentPatch.Builder> operator) {
		return components(base, operator.apply(DataComponentPatch.builder()).build());
	}

	public static Ingredient components(ItemStack stack) {
		Objects.requireNonNull(stack, "Stack cannot be null");

		return components(Ingredient.of(stack.getItem()), stack.getComponentsPatch());
	}

	public static Ingredient customData(Ingredient base, CompoundTag nbt) {
		return new CustomDataIngredient(base, nbt).toVanilla();
	}

	private DefaultCustomIngredients() {
	}
}

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
package com.bookkeepersmc.notebook.impl.recipe.builtin;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Holder;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredientSerializer;

public class AllIngredient extends CombinedIngredient {
	private static final MapCodec<AllIngredient> CODEC = Ingredient.ALLOW_EMPTY_CODEC
			.listOf()
			.fieldOf("ingredients")
			.xmap(AllIngredient::new, AllIngredient::getIngredients);

	public static final CustomIngredientSerializer<AllIngredient> SERIALIZER =
			new Serializer<>(Identifier.of("notebook", "all"), AllIngredient::new, CODEC);

	public AllIngredient(List<Ingredient> ingredients) {
		super(ingredients);
	}

	@Override
	public boolean test(ItemStack stack) {
		for (Ingredient ingredient : ingredients) {
			if (!ingredient.test(stack)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public List<Holder<Item>> getMatchingStacks() {
		// There's always at least one sub ingredient, so accessing ingredients[0] is safe.
		List<Holder<Item>> previewStacks = new ArrayList<>(ingredients.getFirst().method_8105());

		for (int i = 1; i < ingredients.size(); ++i) {
			Ingredient ing = ingredients.get(i);
			previewStacks.removeIf(entry -> !ing.test(entry.getValue().getDefaultStack()));
		}

		return previewStacks;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}

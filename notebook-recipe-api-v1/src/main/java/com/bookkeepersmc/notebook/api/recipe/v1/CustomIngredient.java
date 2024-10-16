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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderSet;

import com.bookkeepersmc.notebook.impl.recipe.CustomIngredientImpl;

public interface CustomIngredient {

	boolean test(ItemStack stack);

	List<Holder<Item>> getItems();

	boolean requiresTesting();

	CustomIngredientSerializer<?> getSerializer();

	@ApiStatus.NonExtendable
	default Ingredient toVanilla() {
		return new CustomIngredientImpl(this);
	}

	/**
	 * Returns a {@link SlotDisplay} representing this ingredient, this is synced to the client to display in the recipe book.
	 *
	 * @return a {@link SlotDisplay} instance.
	 */
	default SlotDisplay getSlotDisplay() {
		return HolderSet.createDirect(getItems()).getTagOrContents().map(
				SlotDisplay.TagSlotDisplay::new,
				(itemEntries) -> new SlotDisplay.CompositeSlotDisplay(
						itemEntries.stream().map(Ingredient::createSingleItemDisplay).toList()
				)
		);
	}
}

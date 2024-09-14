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
package com.bookkeepersmc.notebook.mixin.itemgroup;

import static net.minecraft.item.ItemGroups.BUILDING_BLOCKS;
import static net.minecraft.item.ItemGroups.COLORED_BLOCKS;
import static net.minecraft.item.ItemGroups.COMBAT;
import static net.minecraft.item.ItemGroups.FOOD_AND_DRINKS;
import static net.minecraft.item.ItemGroups.FUNCTIONAL_BLOCKS;
import static net.minecraft.item.ItemGroups.INGREDIENTS;
import static net.minecraft.item.ItemGroups.NATURAL_BLOCKS;
import static net.minecraft.item.ItemGroups.OPERATOR_UTILITIES;
import static net.minecraft.item.ItemGroups.REDSTONE_BLOCKS;
import static net.minecraft.item.ItemGroups.SAVED_HOTBARS;
import static net.minecraft.item.ItemGroups.SEARCH_ITEMS;
import static net.minecraft.item.ItemGroups.SPAWN_EGGS;
import static net.minecraft.item.ItemGroups.SURVIVAL_INVENTORY;
import static net.minecraft.item.ItemGroups.TOOLS_AND_UTILITIES;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.Holder;
import net.minecraft.registry.ResourceKey;

import com.bookkeepersmc.notebook.impl.itemgroup.NotebookItemGroupImpl;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {
	@Unique
	private static final int TABS_PER_PAGE = NotebookItemGroupImpl.TABS_PER_PAGE;

	@Inject(method = "bootstrap", at = @At("HEAD"), cancellable = true)
	private static void deferDuplicateCheck(CallbackInfo ci) {
		/*
		 * Defer the duplication checks to when fabric performs them (see mixin below).
		 * It is preserved just in case, but fabric's pagination logic should prevent any from happening anyway.
		 */
		ci.cancel();
	}

	@Inject(method = "reloadEntries", at = @At("TAIL"))
	private static void paginateGroups(CallbackInfo ci) {
		final List<ResourceKey<ItemGroup>> vanillaGroups = List.of(BUILDING_BLOCKS, COLORED_BLOCKS, NATURAL_BLOCKS, FUNCTIONAL_BLOCKS, REDSTONE_BLOCKS, SAVED_HOTBARS, SEARCH_ITEMS, TOOLS_AND_UTILITIES, COMBAT, FOOD_AND_DRINKS, INGREDIENTS, SPAWN_EGGS, OPERATOR_UTILITIES, SURVIVAL_INVENTORY);

		int count = 0;

		Comparator<Holder.Reference<ItemGroup>> entryComparator = (e1, e2) -> {
			// Non-displayable groups should come last for proper pagination
			int displayCompare = Boolean.compare(e1.value().shouldDisplay(), e2.value().shouldDisplay());

			if (displayCompare != 0) {
				return -displayCompare;
			} else {
				// Ensure a deterministic order
				return e1.getRegistryKey().getValue().compareTo(e2.getRegistryKey().getValue());
			}
		};
		final List<Holder.Reference<ItemGroup>> sortedItemGroups = BuiltInRegistries.ITEM_GROUP.holders()
				.sorted(entryComparator)
				.toList();

		for (Holder.Reference<ItemGroup> reference : sortedItemGroups) {
			final ItemGroup itemGroup = reference.value();
			final NotebookItemGroupImpl fabricItemGroup = (NotebookItemGroupImpl) itemGroup;

			if (vanillaGroups.contains(reference.getRegistryKey())) {
				// Vanilla group goes on the first page.
				fabricItemGroup.notebook_setPage(0);
				continue;
			}

			final ItemGroupAccessor itemGroupAccessor = (ItemGroupAccessor) itemGroup;
			fabricItemGroup.notebook_setPage((count / TABS_PER_PAGE) + 1);
			int pageIndex = count % TABS_PER_PAGE;
			ItemGroup.VerticalPosition row = pageIndex < (TABS_PER_PAGE / 2) ? ItemGroup.VerticalPosition.TOP : ItemGroup.VerticalPosition.BOTTOM;
			itemGroupAccessor.setLocation(row);
			itemGroupAccessor.setColumn(row == ItemGroup.VerticalPosition.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE));

			count++;
		}

		// Overlapping group detection logic, with support for pages.
		record ItemGroupPosition(ItemGroup.VerticalPosition row, int column, int page) { }
		var map = new HashMap<ItemGroupPosition, String>();

		for (ResourceKey<ItemGroup> registryKey : BuiltInRegistries.ITEM_GROUP.getKeys()) {
			final ItemGroup itemGroup = BuiltInRegistries.ITEM_GROUP.method_31140(registryKey);
			final NotebookItemGroupImpl notebookItemGroup = (NotebookItemGroupImpl) itemGroup;
			final String displayName = itemGroup.getName().getString();
			final var position = new ItemGroupPosition(itemGroup.getLocation(), itemGroup.getColumn(), notebookItemGroup.notebook_getPage());
			final String existingName = map.put(position, displayName);

			if (existingName != null) {
				throw new IllegalArgumentException("Duplicate position: (%s) for item groups %s vs %s".formatted(position, displayName, existingName));
			}
		}
	}
}

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
package com.bookkeepersmc.notebook.api.client.itemgroup.v1;

import java.util.List;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;

/**
 * Provided extensions to {@link CreativeInventoryScreen}.
 * This interface is automatically implemented on all creative inventory screens via Mixin and interface injection.
 */
public interface CreativeInventoryScreenExtensions {
	/**
	 * Switches to the page with the given index if it exists.
	 *
	 * @param page the index of the page to switch to
	 * @return Returns true when the page was changed
	 */
	default boolean switchToPage(int page) {
		throw new AssertionError("Implemented by mixin");
	}

	/**
	 * Switches to the next page if it exists.
	 *
	 * @return Returns true when the page was changed
	 */
	default boolean switchToNextPage() {
		return switchToPage(getCurrentPage() + 1);
	}

	/**
	 * Switches to the previous page if it exists.
	 *
	 * @return Returns true when the page was changed
	 */
	default boolean switchToPreviousPage() {
		return switchToPage(getCurrentPage() - 1);
	}

	/**
	 * Returns the index of the current page.
	 */
	default int getCurrentPage() {
		throw new AssertionError("Implemented by mixin");
	}

	/**
	 * Returns the total number of pages.
	 */
	default int getPageCount() {
		throw new AssertionError("Implemented by mixin");
	}

	/**
	 * Returns an ordered list containing the item groups on the requested page.
	 */
	default List<ItemGroup> getItemGroupsOnPage(int page) {
		throw new AssertionError("Implemented by mixin");
	}

	/**
	 * Returns the page index of the given item group.
	 *
	 * <p>Item groups appearing on every page always return the current page index.
	 *
	 * @param itemGroup the item group to get the page index for
	 * @return the page index of the item group
	 */
	default int getPage(ItemGroup itemGroup) {
		throw new AssertionError("Implemented by mixin");
	}

	/**
	 * Returns whether there are additional pages to show on top of the default vanilla pages.
	 *
	 * @return true if there are additional pages
	 */
	default boolean hasAdditionalPages() {
		throw new AssertionError("Implemented by mixin");
	}

	/**
	 * Returns the {@link ItemGroup} that is associated with the currently selected tab.
	 *
	 * @return the currently selected {@link ItemGroup}
	 */
	default ItemGroup getSelectedItemGroup() {
		throw new AssertionError("Implemented by mixin");
	}

	/**
	 * Sets the currently selected tab to the given {@link ItemGroup}.
	 *
	 * @param itemGroup the {@link ItemGroup} to select
	 * @return true if the tab was successfully selected
	 */
	default boolean setSelectedItemGroup(ItemGroup itemGroup) {
		throw new AssertionError("Implemented by mixin");
	}
}

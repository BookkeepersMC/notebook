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
package com.bookkeepersmc.notebook.api.itemgroup.v1;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/**
 * This class allows the entries of {@linkplain ItemGroup item groups} to be modified by the events in {@link ItemGroupEvents}.
 */
public class NotebookItemGroupEntries implements ItemGroup.ItemStackCollector {
	private final ItemGroup.DisplayParameters parameters;
	private final List<ItemStack> displayStacks;
	private final List<ItemStack> searchTabStacks;

	@ApiStatus.Internal
	public NotebookItemGroupEntries(ItemGroup.DisplayParameters context, List<ItemStack> displayStacks, List<ItemStack> searchTabStacks) {
		this.parameters = context;
		this.displayStacks = displayStacks;
		this.searchTabStacks = searchTabStacks;
	}

	public ItemGroup.DisplayParameters getParameters() {
		return parameters;
	}

	/**
	 * @return the currently enabled feature set
	 */
	public FeatureFlagBitSet getEnabledFeatures() {
		return parameters.enabledFeatures();
	}

	/**
	 * @return whether to show items restricted to operators, such as command blocks
	 */
	public boolean shouldShowOpRestrictedItems() {
		return parameters.hasPermissions();
	}

	/**
	 * @return the stacks that will be shown in the tab in the creative mode inventory
	 * @apiNote This list can be modified.
	 */
	public List<ItemStack> getDisplayStacks() {
		return displayStacks;
	}

	/**
	 * @return the stacks that will be searched by the creative mode inventory search
	 * @apiNote This list can be modified.
	 */
	public List<ItemStack> getSearchTabStacks() {
		return searchTabStacks;
	}

	/**
	 * Adds a stack to the end of the item group. Duplicate stacks will be removed.
	 *
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	@Override
	public void addStack(ItemStack stack, ItemGroup.Visibility visibility) {
		if (isEnabled(stack)) {
			checkStack(stack);

			switch (visibility) {
			case PARENT_AND_SEARCH_TABS -> {
				this.displayStacks.add(stack);
				this.searchTabStacks.add(stack);
			}
			case PARENT_TAB_ONLY -> this.displayStacks.add(stack);
			case SEARCH_TAB_ONLY -> this.searchTabStacks.add(stack);
			}
		}
	}

	/**
	 * See {@link #prepend(ItemStack, ItemGroup.Visibility)}. Will use {@link ItemGroup.Visibility#PARENT_AND_SEARCH_TABS}
	 * for visibility.
	 */
	public void prepend(ItemStack stack) {
		prepend(stack, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * Adds a stack to the beginning of the item group. Duplicate stacks will be removed.
	 *
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void prepend(ItemStack stack, ItemGroup.Visibility visibility) {
		if (isEnabled(stack)) {
			checkStack(stack);

			switch (visibility) {
			case PARENT_AND_SEARCH_TABS -> {
				this.displayStacks.add(0, stack);
				this.searchTabStacks.add(0, stack);
			}
			case PARENT_TAB_ONLY -> this.displayStacks.add(0, stack);
			case SEARCH_TAB_ONLY -> this.searchTabStacks.add(0, stack);
			}
		}
	}

	/**
	 * See {@link #prepend(ItemStack)}. Automatically creates an {@link ItemStack} from the given item.
	 */
	public void prepend(ItemConvertible item) {
		prepend(item, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * See {@link #prepend(ItemStack, net.minecraft.item.ItemGroup.Visibility)}.
	 * Automatically creates an {@link ItemStack} from the given item.
	 */
	public void prepend(ItemConvertible item, ItemGroup.Visibility visibility) {
		prepend(new ItemStack(item), visibility);
	}

	/**
	 * See {@link #addAfter(ItemConvertible, Collection)}.
	 */
	public void addAfter(ItemConvertible afterLast, ItemStack... newStack) {
		addAfter(afterLast, Arrays.asList(newStack));
	}

	/**
	 * See {@link #addAfter(ItemStack, Collection)}.
	 */
	public void addAfter(ItemStack afterLast, ItemStack... newStack) {
		addAfter(afterLast, Arrays.asList(newStack));
	}

	/**
	 * See {@link #addAfter(ItemConvertible, Collection)}.
	 */
	public void addAfter(ItemConvertible afterLast, ItemConvertible... newItem) {
		addAfter(afterLast, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #addAfter(ItemStack, Collection)}.
	 */
	public void addAfter(ItemStack afterLast, ItemConvertible... newItem) {
		addAfter(afterLast, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #addAfter(ItemConvertible, Collection, net.minecraft.item.ItemGroup.Visibility)}.
	 */
	public void addAfter(ItemConvertible afterLast, Collection<ItemStack> newStacks) {
		addAfter(afterLast, newStacks, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * See {@link #addAfter(ItemStack, Collection, net.minecraft.item.ItemGroup.Visibility)}.
	 */
	public void addAfter(ItemStack afterLast, Collection<ItemStack> newStacks) {
		addAfter(afterLast, newStacks, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * Adds stacks after an existing item in the group, or at the end, if the item isn't in the group.
	 *
	 * @param afterLast  Add {@code newStacks} after the last entry of this item in the group.
	 * @param newStacks  The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void addAfter(ItemConvertible afterLast, Collection<ItemStack> newStacks, ItemGroup.Visibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			addAfter(afterLast, newStacks, displayStacks);
			addAfter(afterLast, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> addAfter(afterLast, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> addAfter(afterLast, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks after an existing stack in the group, or at the end, if the stack isn't in the group.
	 *
	 * @param afterLast  Add {@code newStacks} after the last group entry matching this stack (compared using {@link ItemStack#itemsAndComponentsMatch}).
	 * @param newStacks  The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void addAfter(ItemStack afterLast, Collection<ItemStack> newStacks, ItemGroup.Visibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			addAfter(afterLast, newStacks, displayStacks);
			addAfter(afterLast, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> addAfter(afterLast, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> addAfter(afterLast, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks after the last group entry matching a predicate, or at the end, if no entries match.
	 *
	 * @param afterLast  Add {@code newStacks} after the last group entry matching this predicate.
	 * @param newStacks  The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void addAfter(Predicate<ItemStack> afterLast, Collection<ItemStack> newStacks, ItemGroup.Visibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			addAfter(afterLast, newStacks, displayStacks);
			addAfter(afterLast, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> addAfter(afterLast, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> addAfter(afterLast, newStacks, searchTabStacks);
		}
	}

	/**
	 * See {@link #addBefore(ItemConvertible, Collection)}.
	 */
	public void addBefore(ItemConvertible beforeFirst, ItemStack... newStack) {
		addBefore(beforeFirst, Arrays.asList(newStack));
	}

	/**
	 * See {@link #addBefore(ItemStack, Collection)}.
	 */
	public void addBefore(ItemStack beforeFirst, ItemStack... newStack) {
		addBefore(beforeFirst, Arrays.asList(newStack));
	}

	/**
	 * See {@link #addBefore(ItemConvertible, Collection)}.
	 */
	public void addBefore(ItemConvertible beforeFirst, ItemConvertible... newItem) {
		addBefore(beforeFirst, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #addBefore(ItemStack, Collection)}.
	 */
	public void addBefore(ItemStack beforeFirst, ItemConvertible... newItem) {
		addBefore(beforeFirst, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #addBefore(ItemConvertible, Collection, net.minecraft.item.ItemGroup.Visibility)}.
	 */
	public void addBefore(ItemConvertible beforeFirst, Collection<ItemStack> newStacks) {
		addBefore(beforeFirst, newStacks, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * See {@link #addBefore(ItemStack, Collection, net.minecraft.item.ItemGroup.Visibility)}.
	 */
	public void addBefore(ItemStack beforeFirst, Collection<ItemStack> newStacks) {
		addBefore(beforeFirst, newStacks, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * Adds stacks before an existing item in the group, or at the end, if the item isn't in the group.
	 *
	 * @param beforeFirst Add {@code newStacks} before the first entry of this item in the group.
	 * @param newStacks   The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility  Determines whether the stack will be shown in the tab itself, returned
	 *                    for searches, or both.
	 */
	public void addBefore(ItemConvertible beforeFirst, Collection<ItemStack> newStacks, ItemGroup.Visibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			addBefore(beforeFirst, newStacks, displayStacks);
			addBefore(beforeFirst, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> addBefore(beforeFirst, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> addBefore(beforeFirst, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks before an existing stack to the group, or at the end, if the stack isn't in the group.
	 *
	 * @param beforeFirst Add {@code newStacks} before the first group entry matching this stack (compared using {@link ItemStack#itemsAndComponentsMatch}).
	 * @param newStacks   The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility  Determines whether the stack will be shown in the tab itself, returned
	 *                    for searches, or both.
	 */
	public void addBefore(ItemStack beforeFirst, Collection<ItemStack> newStacks, ItemGroup.Visibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			addBefore(beforeFirst, newStacks, displayStacks);
			addBefore(beforeFirst, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> addBefore(beforeFirst, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> addBefore(beforeFirst, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks before the first group entry matching a predicate, or at the end, if no entries match.
	 *
	 * @param beforeFirst Add {@code newStacks} before the first group entry matching this predicate.
	 * @param newStacks   The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility  Determines whether the stack will be shown in the tab itself, returned
	 *                    for searches, or both.
	 */
	public void addBefore(Predicate<ItemStack> beforeFirst, Collection<ItemStack> newStacks, ItemGroup.Visibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			addBefore(beforeFirst, newStacks, displayStacks);
			addBefore(beforeFirst, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> addBefore(beforeFirst, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> addBefore(beforeFirst, newStacks, searchTabStacks);
		}
	}

	/**
	 * @return True if the item of a given stack is enabled in the current {@link FeatureFlagBitSet}.
	 * @see Item#enabledIn
	 */
	private boolean isEnabled(ItemStack stack) {
		return stack.getItem().enabledIn(getEnabledFeatures());
	}

	private Collection<ItemStack> getEnabledStacks(Collection<ItemStack> newStacks) {
		// If not all stacks are enabled, filter the list, otherwise use it as-is
		if (newStacks.stream().allMatch(this::isEnabled)) {
			return newStacks;
		}

		return newStacks.stream().filter(this::isEnabled).toList();
	}

	/**
	 * Adds the {@link ItemStack} before the first match, if no matches the {@link ItemStack} is appended to the end of the {@link ItemGroup}.
	 */
	private static void addBefore(Predicate<ItemStack> predicate, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		for (int i = 0; i < addTo.size(); i++) {
			if (predicate.test(addTo.get(i))) {
				addTo.subList(i, i).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void addAfter(Predicate<ItemStack> predicate, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		// Iterate in reverse to add after the last match
		for (int i = addTo.size() - 1; i >= 0; i--) {
			if (predicate.test(addTo.get(i))) {
				addTo.subList(i + 1, i + 1).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void addBefore(ItemStack anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		for (int i = 0; i < addTo.size(); i++) {
			if (ItemStack.itemsAndComponentsMatch(anchor, addTo.get(i))) {
				addTo.subList(i, i).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void addAfter(ItemStack anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		// Iterate in reverse to add after the last match
		for (int i = addTo.size() - 1; i >= 0; i--) {
			if (ItemStack.itemsAndComponentsMatch(anchor, addTo.get(i))) {
				addTo.subList(i + 1, i + 1).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void addBefore(ItemConvertible anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		Item anchorItem = anchor.asItem();

		for (int i = 0; i < addTo.size(); i++) {
			if (addTo.get(i).isOf(anchorItem)) {
				addTo.subList(i, i).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void addAfter(ItemConvertible anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		Item anchorItem = anchor.asItem();

		// Iterate in reverse to add after the last match
		for (int i = addTo.size() - 1; i >= 0; i--) {
			if (addTo.get(i).isOf(anchorItem)) {
				addTo.subList(i + 1, i + 1).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void checkStacks(Collection<ItemStack> stacks) {
		for (ItemStack stack : stacks) {
			checkStack(stack);
		}
	}

	private static void checkStack(ItemStack stack) {
		if (stack.isEmpty()) {
			throw new IllegalArgumentException("Cannot add empty stack");
		}

		if (stack.getCount() != 1) {
			throw new IllegalArgumentException("Stack size must be exactly 1 for stack: " + stack);
		}
	}
}

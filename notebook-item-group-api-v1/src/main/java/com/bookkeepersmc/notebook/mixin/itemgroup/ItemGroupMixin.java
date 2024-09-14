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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.ResourceKey;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.itemgroup.v1.ItemGroupEvents;
import com.bookkeepersmc.notebook.api.itemgroup.v1.NotebookItemGroupEntries;
import com.bookkeepersmc.notebook.impl.itemgroup.ItemGroupEventsImpl;
import com.bookkeepersmc.notebook.impl.itemgroup.NotebookItemGroupImpl;

@Mixin(ItemGroup.class)
abstract class ItemGroupMixin implements NotebookItemGroupImpl {
	@Shadow
	private Collection<ItemStack> cachedTabStacks;

	@Shadow
	private Set<ItemStack> cachedSearchStacks;

	@Unique
	private int page = -1;

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "reload", at = @At("TAIL"))
	public void getStacks(ItemGroup.DisplayParameters context, CallbackInfo ci) {
		final ItemGroup self = (ItemGroup) (Object) this;
		final ResourceKey<ItemGroup> resourceKey = BuiltInRegistries.ITEM_GROUP.getKey(self).orElseThrow(() -> new IllegalStateException("Unregistered item group : " + self));

		// Do not modify special item groups (except Operator Blocks) at all.
		// Special item groups include Saved Hotbars, Search, and Survival Inventory.
		// Note, search gets modified as part of the parent item group.
		if (self.isSpecial() && resourceKey != ItemGroups.OPERATOR_UTILITIES) return;

		// Sanity check for the injection point. It should be after these fields are set.
		Objects.requireNonNull(cachedTabStacks, "displayStacks");
		Objects.requireNonNull(cachedSearchStacks, "searchTabStacks");

		// Convert the entries to lists
		var mutableDisplayStacks = new LinkedList<>(cachedTabStacks);
		var mutableSearchTabStacks = new LinkedList<>(cachedSearchStacks);
		var entries = new NotebookItemGroupEntries(context, mutableDisplayStacks, mutableSearchTabStacks);

		// Now trigger the events
		if (resourceKey != ItemGroups.OPERATOR_UTILITIES || context.hasPermissions()) {
			final Event<ItemGroupEvents.ModifyEntries> modifyEntriesEvent = ItemGroupEventsImpl.getModifyEntriesEvent(resourceKey);

			if (modifyEntriesEvent != null) {
				modifyEntriesEvent.invoker().modifyEntries(entries);
			}

			ItemGroupEvents.MODIFY_ENTRIES_ALL.invoker().modifyEntries(self, entries);
		}

		// Convert the stacks back to sets after the events had a chance to modify them
		cachedTabStacks.clear();
		cachedTabStacks.addAll(mutableDisplayStacks);

		cachedSearchStacks.clear();
		cachedSearchStacks.addAll(mutableSearchTabStacks);
	}

	@Override
	public int notebook_getPage() {
		if (page < 0) {
			throw new IllegalStateException("Item group has no page");
		}

		return page;
	}

	@Override
	public void notebook_setPage(int page) {
		this.page = page;
	}
}

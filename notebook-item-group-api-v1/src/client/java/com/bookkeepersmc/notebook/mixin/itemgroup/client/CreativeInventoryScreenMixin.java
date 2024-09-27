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
package com.bookkeepersmc.notebook.mixin.itemgroup.client;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.text.Text;

import com.bookkeepersmc.notebook.api.client.itemgroup.v1.CreativeInventoryScreenExtensions;
import com.bookkeepersmc.notebook.impl.client.itemgroup.NotebookCreativeGuiComponents;
import com.bookkeepersmc.notebook.impl.itemgroup.NotebookItemGroupImpl;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreen<CreativeScreenHandler> implements CreativeInventoryScreenExtensions {
	public CreativeInventoryScreenMixin(CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
		super(screenHandler, playerInventory, text);
	}

	@Shadow
	protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	@Shadow
	private static ItemGroup selectedTab;

	// "static" matches selectedTab
	@Unique
	private static int currentPage = 0;

	@Unique
	private void updateSelection() {
		if (!isGroupVisible(selectedTab)) {
			ItemGroups.getAllGroups()
					.stream()
					.filter(this::isGroupVisible)
					.min((a, b) -> Boolean.compare(a.isSpecial(), b.isSpecial()))
					.ifPresent(this::setSelectedTab);
		}
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void init(CallbackInfo info) {
		currentPage = getPage(selectedTab);

		int xpos = x + 170;
		int ypos = y + 4;

		CreativeInventoryScreen self = (CreativeInventoryScreen) (Object) this;
		addDrawable(new NotebookCreativeGuiComponents.ItemGroupButtonWidget(xpos + 11, ypos, NotebookCreativeGuiComponents.Type.NEXT, self));
		addDrawable(new NotebookCreativeGuiComponents.ItemGroupButtonWidget(xpos, ypos, NotebookCreativeGuiComponents.Type.PREVIOUS, self));
	}

	@Inject(method = "setSelectedTab", at = @At("HEAD"), cancellable = true)
	private void setSelectedTab(ItemGroup itemGroup, CallbackInfo info) {
		if (!isGroupVisible(itemGroup)) {
			info.cancel();
		}
	}

	@Inject(method = "renderTabTooltipIfHovered", at = @At("HEAD"), cancellable = true)
	private void renderTabTooltipIfHovered(GuiGraphics guiGraphics, ItemGroup itemGroup, int mx, int my, CallbackInfoReturnable<Boolean> info) {
		if (!isGroupVisible(itemGroup)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "isClickInTab", at = @At("HEAD"), cancellable = true)
	private void isClickInTab(ItemGroup itemGroup, double mx, double my, CallbackInfoReturnable<Boolean> info) {
		if (!isGroupVisible(itemGroup)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "renderTabIcon", at = @At("HEAD"), cancellable = true)
	private void renderTabIcon(GuiGraphics guiGraphics, ItemGroup itemGroup, CallbackInfo info) {
		if (!isGroupVisible(itemGroup)) {
			info.cancel();
		}
	}

	@Unique
	private boolean isGroupVisible(ItemGroup itemGroup) {
		return itemGroup.shouldDisplay() && currentPage == getPage(itemGroup);
	}

	@Override
	public int getPage(ItemGroup itemGroup) {
		if (NotebookCreativeGuiComponents.COMMON_GROUPS.contains(itemGroup)) {
			return currentPage;
		}

		final NotebookItemGroupImpl fabricItemGroup = (NotebookItemGroupImpl) itemGroup;
		return fabricItemGroup.notebook_getPage();
	}

	@Unique
	private boolean hasGroupForPage(int page) {
		return ItemGroups.getDisplayableGroups()
				.stream()
				.anyMatch(itemGroup -> getPage(itemGroup) == page);
	}

	@Override
	public boolean switchToPage(int page) {
		if (!hasGroupForPage(page)) {
			return false;
		}

		if (currentPage == page) {
			return false;
		}

		currentPage = page;
		updateSelection();
		return true;
	}

	@Override
	public int getCurrentPage() {
		return currentPage;
	}

	@Override
	public int getPageCount() {
		return NotebookCreativeGuiComponents.getPageCount();
	}

	@Override
	public List<ItemGroup> getItemGroupsOnPage(int page) {
		return ItemGroups.getAllGroups()
				.stream()
				.filter(itemGroup -> getPage(itemGroup) == page)
				// Thanks to isXander for the sorting
				.sorted(Comparator.comparing(ItemGroup::getLocation).thenComparingInt(ItemGroup::getColumn))
				.sorted((a, b) -> {
					if (a.isSpecial() && !b.isSpecial()) return 1;
					if (!a.isSpecial() && b.isSpecial()) return -1;
					return 0;
				})
				.toList();
	}

	@Override
	public boolean hasAdditionalPages() {
		return ItemGroups.getDisplayableGroups().size() > (Objects.requireNonNull(ItemGroups.cachedParameters).hasPermissions() ? 14 : 13);
	}

	@Override
	public ItemGroup getSelectedItemGroup() {
		return selectedTab;
	}

	@Override
	public boolean setSelectedItemGroup(ItemGroup itemGroup) {
		Objects.requireNonNull(itemGroup, "itemGroup");

		if (selectedTab == itemGroup) {
			return false;
		}

		if (currentPage != getPage(itemGroup)) {
			if (!switchToPage(getPage(itemGroup))) {
				return false;
			}
		}

		setSelectedTab(itemGroup);
		return true;
	}
}

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
package com.bookkeepersmc.notebook.impl.client.itemgroup;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.itemgroup.NotebookItemGroupImpl;

public class NotebookCreativeGuiComponents {
	private static final Identifier BUTTON_TEX = Identifier.of("fabric", "textures/gui/creative_buttons.png");
	private static final double TABS_PER_PAGE = NotebookItemGroupImpl.TABS_PER_PAGE;
	public static final Set<ItemGroup> COMMON_GROUPS = Set.of(ItemGroups.SEARCH_ITEMS, ItemGroups.SURVIVAL_INVENTORY, ItemGroups.SAVED_HOTBARS).stream()
			.map(BuiltInRegistries.ITEM_GROUP::method_31140)
			.collect(Collectors.toSet());

	public static int getPageCount() {
		return (int) Math.ceil((ItemGroups.getDisplayableGroups().size() - COMMON_GROUPS.size()) / TABS_PER_PAGE);
	}

	public static class ItemGroupButtonWidget extends ButtonWidget {
		final CreativeInventoryScreen screen;
		final Type type;

		public ItemGroupButtonWidget(int x, int y, Type type, CreativeInventoryScreen screen) {
			super(x, y, 11, 12, type.text, (bw) -> type.clickConsumer.accept(screen), ButtonWidget.DEFAULT_NARRATION);
			this.type = type;
			this.screen = screen;
		}

		@Override
		protected void drawWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
			this.active = type.isEnabled.test(screen);
			this.visible = screen.hasAdditionalPages();

			if (!this.visible) {
				return;
			}

			int u = active && this.isHovered() ? 22 : 0;
			int v = active ? 0 : 12;
			guiGraphics.method_25290(RenderLayer::getGuiTextured, BUTTON_TEX, this.getX(), this.getY(), u + (type == Type.NEXT ? 11 : 0), v, 11, 12, 256, 256);

			if (this.isHovered()) {
				guiGraphics.drawTooltip(Minecraft.getInstance().textRenderer, Text.translatable("notebook.gui.creativeTabPage", screen.getCurrentPage() + 1, getPageCount()), mouseX, mouseY);
			}
		}
	}

	public enum Type {
		NEXT(Text.literal(">"), CreativeInventoryScreen::switchToNextPage, screen -> screen.getCurrentPage() + 1 < screen.getPageCount()),
		PREVIOUS(Text.literal("<"), CreativeInventoryScreen::switchToPreviousPage, screen -> screen.getCurrentPage() != 0);

		final Text text;
		final Consumer<CreativeInventoryScreen> clickConsumer;
		final Predicate<CreativeInventoryScreen> isEnabled;

		Type(Text text, Consumer<CreativeInventoryScreen> clickConsumer, Predicate<CreativeInventoryScreen> isEnabled) {
			this.text = text;
			this.clickConsumer = clickConsumer;
			this.isEnabled = isEnabled;
		}
	}
}

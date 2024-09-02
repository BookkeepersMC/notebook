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
package com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.ModListWidget;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.ModSearch;

public class ParentEntry extends ModListEntry {
	private static final Identifier PARENT_MOD_TEXTURE = Identifier.of(NotebookModScreen.MOD_ID, "textures/gui/parent_mod.png");
	protected List<Mod> children;
	protected ModListWidget list;
	protected boolean hoveringIcon = false;

	public ParentEntry(Mod parent, List<Mod> children, ModListWidget list) {
		super(parent, list);
		this.children = children;
		this.list = list;
	}

	@Override
	public void render(
		GuiGraphics guiGraphics,
		int index,
		int y,
		int x,
		int rowWidth,
		int rowHeight,
		int mouseX,
		int mouseY,
		boolean isSelected,
		float delta
	) {
		super.render(guiGraphics, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		TextRenderer font = client.textRenderer;
		int childrenBadgeHeight = font.fontHeight;
		int childrenBadgeWidth = font.fontHeight;
		int shownChildren = ModSearch.search(list.getParent(), list.getParent().getSearchInput(), getChildren()).size();
		Text str = shownChildren == children.size() ?
			Text.literal(String.valueOf(shownChildren)) :
			Text.literal(shownChildren + "/" + children.size());
		int childrenWidth = font.getWidth(str) - 1;
		if (childrenBadgeWidth < childrenWidth + 4) {
			childrenBadgeWidth = childrenWidth + 4;
		}
		int iconSize = ModScreenConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		int childrenBadgeX = x + iconSize - childrenBadgeWidth;
		int childrenBadgeY = y + iconSize - childrenBadgeHeight;
		int childrenOutlineColor = 0xff107454;
		int childrenFillColor = 0xff093929;
		guiGraphics.fill(childrenBadgeX + 1,
			childrenBadgeY,
			childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + 1,
			childrenOutlineColor
		);
		guiGraphics.fill(childrenBadgeX,
			childrenBadgeY + 1,
			childrenBadgeX + 1,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenOutlineColor
		);
		guiGraphics.fill(childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + 1,
			childrenBadgeX + childrenBadgeWidth,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenOutlineColor
		);
		guiGraphics.fill(childrenBadgeX + 1,
			childrenBadgeY + 1,
			childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenFillColor
		);
		guiGraphics.fill(childrenBadgeX + 1,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + childrenBadgeHeight,
			childrenOutlineColor
		);
		guiGraphics.drawText(font,
			str.asOrderedText(),
			(int) (childrenBadgeX + (float) childrenBadgeWidth / 2 - (float) childrenWidth / 2),
			childrenBadgeY + 1,
			0xCACACA,
			false
		);
		this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + iconSize && mouseY >= y - 1 && mouseY <= y - 1 + iconSize;
		if (isMouseOver(mouseX, mouseY)) {
			guiGraphics.fill(x, y, x + iconSize, y + iconSize, 0xA0909090);
			int xOffset = list.getParent().showModChildren.contains(getMod().getId()) ? iconSize : 0;
			int yOffset = hoveringIcon ? iconSize : 0;
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.method_25290(RenderLayer::getGuiTextured, PARENT_MOD_TEXTURE,
				x,
				y,
				xOffset,
				yOffset,
				iconSize + xOffset,
				iconSize + yOffset,
				ModScreenConfig.COMPACT_LIST.getValue() ?
					(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
					256,
				ModScreenConfig.COMPACT_LIST.getValue() ?
					(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
					256
			);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int i) {
		int iconSize = ModScreenConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		boolean quickConfigure = ModScreenConfig.QUICK_CONFIGURE.getValue();
		if (mouseX - list.getRowLeft() <= iconSize) {
			this.toggleChildren();
			return true;
		} else if (!quickConfigure && Util.getMeasuringTimeMs() - this.sinceLastClick < 250) {
			this.toggleChildren();
			return true;
		} else {
			return super.mouseClicked(mouseX, mouseY, i);
		}
	}

	private void toggleChildren() {
		String id = getMod().getId();
		if (list.getParent().showModChildren.contains(id)) {
			list.getParent().showModChildren.remove(id);
		} else {
			list.getParent().showModChildren.add(id);
		}
		list.filter(list.getParent().getSearchInput(), false);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		String modId = getMod().getId();
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
			if (list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.remove(modId);
			} else {
				list.getParent().showModChildren.add(modId);
			}
			list.filter(list.getParent().getSearchInput(), false);
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_LEFT) {
			if (list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.remove(modId);
				list.filter(list.getParent().getSearchInput(), false);
			}
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			if (!list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.add(modId);
				list.filter(list.getParent().getSearchInput(), false);
			} else {
				return list.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0);
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public void setChildren(List<Mod> children) {
		this.children = children;
	}

	public void addChildren(List<Mod> children) {
		this.children.addAll(children);
	}

	public void addChildren(Mod... children) {
		this.children.addAll(Arrays.asList(children));
	}

	public List<Mod> getChildren() {
		return children;
	}

	@Override
	public boolean isMouseOver(double double_1, double double_2) {
		return Objects.equals(this.list.getEntryAtPos(double_1, double_2), this);
	}
}

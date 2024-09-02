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
package com.bookkeepersmc.notebook.impl.mod.screen.util.mod;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.OrderedText;

import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.util.DrawingUtil;

public class ModBadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected Mod mod;
	protected Minecraft client;
	protected final ModsScreen screen;

	public ModBadgeRenderer(int startX, int startY, int endX, Mod mod, ModsScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.mod = mod;
		this.screen = screen;
		this.client = Minecraft.getInstance();
	}

	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		Set<Mod.Badge> badges = mod.getBadges();
		badges.forEach(badge -> drawBadge(guiGraphics, badge, mouseX, mouseY));
	}

	public void drawBadge(GuiGraphics guiGraphics, Mod.Badge badge, int mouseX, int mouseY) {
		this.drawBadge(guiGraphics,
			badge.getText().asOrderedText(),
			badge.getOutlineColor(),
			badge.getFillColor(),
			mouseX,
			mouseY
		);
	}

	public void drawBadge(
		GuiGraphics guiGraphics,
		OrderedText text,
		int outlineColor,
		int fillColor,
		int mouseX,
		int mouseY
	) {
		int width = client.textRenderer.getWidth(text) + 6;
		if (badgeX + width < badgeMax) {
			DrawingUtil.drawBadge(guiGraphics, badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
			badgeX += width + 3;
		}
	}

	public Mod getMod() {
		return mod;
	}
}

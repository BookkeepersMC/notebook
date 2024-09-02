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
package com.bookkeepersmc.notebook.impl.mod.screen.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;

import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;

public class ModScreenButtonWidget extends ButtonWidget {
	public ModScreenButtonWidget(int x, int y, int width, int height, Text text, Screen screen) {
		super(x,
			y,
			width,
			height,
			text,
			button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)),
			ButtonWidget.DEFAULT_NARRATION
		);
	}

	@Override
	public void drawWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.drawWidget(guiGraphics, mouseX, mouseY, delta);
		if (ModScreenConfig.BUTTON_UPDATE_BADGE.getValue() && NotebookModScreen.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(guiGraphics,
				this.width + this.getX() - 16,
				this.height / 2 + this.getY() - 4
			);
		}
	}
}

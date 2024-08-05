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
package com.bookkeepersmc.notebook.impl.mod.screen.util;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;

public class DrawingUtil {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void drawRandomVersionBackground(
		Mod mod,
		GuiGraphics guiGraphics,
		int x,
		int y,
		int width,
		int height
	) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | Mth.hsvToRgb(random.nextFloat(1f), random.nextFloat(0.7f, 0.8f), 0.9f);
		if (!ModScreenConfig.RANDOM_JAVA_COLORS.getValue()) {
			color = 0xFFDD5656;
		}
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		guiGraphics.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(
		GuiGraphics guiGraphics,
		String string,
		int x,
		int y,
		int wrapWidth,
		int lines,
		int color
	) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<FormattedText> strings = CLIENT.font.getSplitter()
			.splitLines(Component.literal(string), wrapWidth, Style.EMPTY);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			FormattedText renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable = FormattedText.composite(strings.get(i), FormattedText.of("..."));
			}
			FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
			int x1 = x;
			if (CLIENT.font.isBidirectional()) {
				int width = CLIENT.font.width(line);
				x1 += (float) (wrapWidth - width);
			}
			guiGraphics.drawString(CLIENT.font, line, x1, y + i * CLIENT.font.lineHeight, color, true);
		}
	}

	public static void drawBadge(
		GuiGraphics guiGraphics,
		int x,
		int y,
		int tagWidth,
		FormattedCharSequence text,
		int outlineColor,
		int fillColor,
		int textColor
	) {
		guiGraphics.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		guiGraphics.fill(x, y, x + 1, y + CLIENT.font.lineHeight, outlineColor);
		guiGraphics.fill(x + 1,
			y + 1 + CLIENT.font.lineHeight - 1,
			x + tagWidth,
			y + CLIENT.font.lineHeight + 1,
			outlineColor
		);
		guiGraphics.fill(x + tagWidth, y, x + tagWidth + 1, y + CLIENT.font.lineHeight, outlineColor);
		guiGraphics.fill(x + 1, y, x + tagWidth, y + CLIENT.font.lineHeight, fillColor);
		guiGraphics.drawString(CLIENT.font,
			text,
			(int) (x + 1 + (tagWidth - CLIENT.font.width(text)) / (float) 2),
			y + 1,
			textColor,
			false
		);
	}
}

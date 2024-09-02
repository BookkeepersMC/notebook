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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.button.TexturedButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LegacyTexturedButtonWidget extends TexturedButtonWidget {
	private final int u;
	private final int v;
	private final int hoveredVOffset;

	private final Identifier texture;

	private final int textureWidth;
	private final int textureHeight;

	public LegacyTexturedButtonWidget(
		int x,
		int y,
		int width,
		int height,
		int u,
		int v,
		int hoveredVOffset,
		Identifier texture,
		int textureWidth,
		int textureHeight,
		ButtonWidget.PressAction pressAction,
		Text message
	) {
		super(x, y, width, height, null, pressAction, message);

		this.u = u;
		this.v = v;
		this.hoveredVOffset = hoveredVOffset;

		this.texture = texture;

		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void drawWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		int v = this.v;

		if (!this.isNarratable()) {
			v += this.hoveredVOffset * 2;
		} else if (this.isHoveredOrFocused()) {
			v += this.hoveredVOffset;
		}

		context.method_25290(RenderLayer::getGuiTextured, this.texture,
			this.getX(),
			this.getY(),
			this.u,
			v,
			this.width,
			this.height,
			this.textureWidth,
			this.textureHeight
		);
	}

	public static Builder legacyTexturedBuilder(Text message, ButtonWidget.PressAction onPress) {
		return new Builder(message, onPress);
	}

	public static class Builder {
		private final Text message;
		private final ButtonWidget.PressAction onPress;

		private int x;
		private int y;

		private int width;
		private int height;

		private int u;
		private int v;
		private int hoveredVOffset;

		private Identifier texture;

		private int textureWidth;
		private int textureHeight;

		public Builder(Text message, PressAction onPress) {
			this.message = message;
			this.onPress = onPress;
		}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;

			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;

			return this;
		}

		public Builder uv(int u, int v, int hoveredVOffset) {
			this.u = u;
			this.v = v;

			this.hoveredVOffset = hoveredVOffset;

			return this;
		}

		public Builder texture(Identifier texture, int textureWidth, int textureHeight) {
			this.texture = texture;

			this.textureWidth = textureWidth;
			this.textureHeight = textureHeight;

			return this;
		}

		public LegacyTexturedButtonWidget build() {
			return new LegacyTexturedButtonWidget(this.x,
				this.y,
				this.width,
				this.height,
				this.u,
				this.v,
				this.hoveredVOffset,
				this.texture,
				this.textureWidth,
				this.textureHeight,
				this.onPress,
				this.message
			);
		}
	}
}

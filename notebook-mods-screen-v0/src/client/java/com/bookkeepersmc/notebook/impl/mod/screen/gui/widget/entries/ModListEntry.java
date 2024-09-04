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

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;

import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.ModListWidget;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.UpdateAvailableBadge;
import com.bookkeepersmc.notebook.impl.mod.screen.util.DrawingUtil;
import com.bookkeepersmc.notebook.impl.mod.screen.util.ModScreenTexts;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.ModBadgeRenderer;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = Identifier.ofDefault("textures/misc/unknown_pack.png");
	private static final Identifier MOD_CONFIGURATION_ICON = Identifier.of(NotebookModScreen.MOD_ID,
		"textures/gui/mod_configuration.png"
	);
	private static final Identifier ERROR_ICON = Identifier.ofDefault("world_list/error");
	private static final Identifier ERROR_HIGHLIGHTED_ICON = Identifier.ofDefault("world_list/error_highlighted");

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = Minecraft.getInstance();
	}

	@Override
	public Text getNarration() {
		return Text.literal(mod.getTranslatedName());
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
		boolean hovered,
		float delta
	) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModScreenConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, guiGraphics, x, y, iconSize, iconSize);
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		guiGraphics.method_25290(RenderLayer::getGuiTextured, this.getIconTexture(), x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		RenderSystem.disableBlend();
		Text name = Text.literal(mod.getTranslatedName());
		StringVisitable trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getWidth(name) > maxNameWidth) {
			StringVisitable ellipsis = StringVisitable.plain("...");
			trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)),
				ellipsis
			);
		}
		guiGraphics.drawText(font,
			Language.getInstance().reorder(trimmedName),
			x + iconSize + 3,
			y + 1,
			0xFFFFFF,
			true
		);
		var updateBadgeXOffset = 0;
		if (ModScreenConfig.UPDATE_CHECKER.getValue() && !ModScreenConfig.DISABLE_UPDATE_CHECKER.getValue()
			.contains(modId) && (mod.hasUpdate() || mod.getChildHasUpdate())) {
			UpdateAvailableBadge.renderBadge(guiGraphics, x + iconSize + 3 + font.getWidth(name) + 2, y);
			updateBadgeXOffset = 11;
		}
		if (!ModScreenConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.getWidth(name) + 2 + updateBadgeXOffset,
				y,
				x + rowWidth,
				mod,
				list.getParent()
			).draw(guiGraphics, mouseX, mouseY);
		}
		if (!ModScreenConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(guiGraphics,
				summary,
				(x + iconSize + 3 + 4),
				(y + client.textRenderer.fontHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0x808080
			);
		} else {
			DrawingUtil.drawWrappedString(guiGraphics,
				mod.getPrefixedVersion(),
				(x + iconSize + 3),
				(y + client.textRenderer.fontHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0x808080
			);
		}

		if (!(this instanceof ParentEntry) && ModScreenConfig.QUICK_CONFIGURE.getValue() && (this.list.getParent()
			.getModHasConfigScreen()
			.get(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModScreenConfig.COMPACT_LIST.getValue() ?
				(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
				256;
			if (this.client.options.getTouchscreen().get() || hovered) {
				guiGraphics.fill(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					guiGraphics.method_52706(RenderLayer::getGuiTextured, hoveringIcon ? ERROR_HIGHLIGHTED_ICON : ERROR_ICON,
						x,
						y,
						iconSize,
						iconSize
					);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						this.list.getParent()
							.setDeferredTooltip(this.client.textRenderer.wrapLines(
								ModScreenTexts.configureError(modId, e),
								175
							));
					}
				} else {
					int v = hoveringIcon ? iconSize : 0;
					guiGraphics.method_25290(RenderLayer::getGuiTextured, MOD_CONFIGURATION_ICON,
						x,
						y,
						0.0F,
						(float) v,
						iconSize,
						iconSize,
						textureSize,
						textureSize
					);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int delta) {
		list.select(this);
		if (ModScreenConfig.QUICK_CONFIGURE.getValue() && this.list.getParent()
			.getModHasConfigScreen()
			.get(this.mod.getId())) {
			int iconSize = ModScreenConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (mouseX - list.getRowLeft() <= iconSize) {
				this.openConfig();
			} else if (Util.getMeasuringTimeMs() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}
		this.sinceLastClick = Util.getMeasuringTimeMs();
		return true;
	}

	public void openConfig() {
		Minecraft.getInstance().setScreen(NotebookModScreen.getConfigScreen(mod.getId(), list.getParent()));
	}

	public Mod getMod() {
		return mod;
	}

	public Identifier getIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = Identifier.of(NotebookModScreen.MOD_ID, mod.getId() + "_icon");
			NativeImageBackedTexture icon = mod.getIcon(list.getIconHandler(),
				64 * this.client.options.getGuiScale().get()
			);
			if (icon != null) {
				this.client.getTextureManager().registerTexture(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		return iconLocation;
	}

	public int getXOffset() {
		return 0;
	}
}

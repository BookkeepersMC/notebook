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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferRenderer;
import com.mojang.blaze3d.vertex.BuiltBuffer;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.list.ElementListWidget;
import net.minecraft.client.gui.widget.list.EntryListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_qrsmnkgy;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateInfo;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries.ModListEntry;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {

	private static final Text HAS_UPDATE_TEXT = Text.translatable("modscreen.hasUpdate");
	private static final Text EXPERIMENTAL_TEXT = Text.translatable("modscreen.experimental").formatted(Formatting.GOLD);
	private static final Text DOWNLOAD_TEXT = Text.translatable("modscreen.downloadLink")
		.formatted(Formatting.BLUE)
		.formatted(Formatting.UNDERLINE);
	private static final Text CHILD_HAS_UPDATE_TEXT = Text.translatable("modscreen.childHasUpdate");
	private static final Text LINKS_TEXT = Text.translatable("modscreen.links");
	private static final Text SOURCE_TEXT = Text.translatable("modscreen.source")
		.formatted(Formatting.BLUE)
		.formatted(Formatting.UNDERLINE);
	private static final Text LICENSE_TEXT = Text.translatable("modscreen.license");
	private static final Text VIEW_CREDITS_TEXT = Text.translatable("modscreen.viewCredits")
		.formatted(Formatting.BLUE)
		.formatted(Formatting.UNDERLINE);
	private static final Text CREDITS_TEXT = Text.translatable("modscreen.credits");

	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(
		Minecraft client,
		int width,
		int height,
		int y,
		int itemHeight,
		ModsScreen parent
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;
	}

	@Override
	public DescriptionEntry getSelectedOrNull() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPositionX() {
		return this.width - 6 + this.getX();
	}

	@Override
	public void updateNarration(NarrationMessageBuilder builder) {
		Mod mod = parent.getSelectedEntry().getMod();
		builder.put(NarrationPart.TITLE, mod.getTranslatedName() + " " + mod.getPrefixedVersion());
	}

	@Override
	public void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			if (lastSelected != null) {
				DescriptionEntry emptyEntry = new DescriptionEntry(OrderedText.EMPTY);
				int wrapWidth = getRowWidth() - 5;

				Mod mod = lastSelected.getMod();
				Text description = mod.getFormattedDescription();
				if (!description.getString().isEmpty()) {
					for (OrderedText line : textRenderer.wrapLines(description, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}
				}

				if (ModScreenConfig.UPDATE_CHECKER.getValue() && !ModScreenConfig.DISABLE_UPDATE_CHECKER.getValue()
					.contains(mod.getId())) {
					UpdateInfo updateInfo = mod.getUpdateInfo();
					if (updateInfo != null && updateInfo.isUpdateAvailable()) {
						children().add(emptyEntry);

						int index = 0;
						for (OrderedText line : textRenderer.wrapLines(HAS_UPDATE_TEXT, wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) {
								entry.setUpdateTextEntry();
							}

							children().add(entry);
							index += 1;
						}

						for (OrderedText line : textRenderer.wrapLines(EXPERIMENTAL_TEXT, wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, 8));
						}


						Text updateMessage = updateInfo.getUpdateMessage();
						String downloadLink = updateInfo.getDownloadLink();
						if (updateMessage == null) {
							updateMessage = DOWNLOAD_TEXT;
						} else {
							if (downloadLink != null) {
								updateMessage = updateMessage.copy()
									.formatted(Formatting.BLUE)
									.formatted(Formatting.UNDERLINE);
							}
						}
						for (OrderedText line : textRenderer.wrapLines(updateMessage, wrapWidth - 16)) {
							if (downloadLink != null) {
								children().add(new LinkEntry(line, downloadLink, 8));
							} else {
								children().add(new DescriptionEntry(line, 8));

							}
						}
					}
					if (mod.getChildHasUpdate()) {
						children().add(emptyEntry);

						int index = 0;
						for (OrderedText line : textRenderer.wrapLines(CHILD_HAS_UPDATE_TEXT, wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) {
								entry.setUpdateTextEntry();
							}

							children().add(entry);
							index += 1;
						}
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModScreenConfig.HIDE_MOD_LINKS.getValue()) {
					children().add(emptyEntry);

					for (OrderedText line : textRenderer.wrapLines(LINKS_TEXT, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (OrderedText line : textRenderer.wrapLines(SOURCE_TEXT, wrapWidth - 16)) {
							children().add(new LinkEntry(line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (OrderedText line : textRenderer.wrapLines(Text.translatable(key)
								.formatted(Formatting.BLUE)
								.formatted(Formatting.UNDERLINE),
							wrapWidth - 16
						)) {
							children().add(new LinkEntry(line, value, indent));
							indent = 16;
						}
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModScreenConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					children().add(emptyEntry);

					for (OrderedText line : textRenderer.wrapLines(LICENSE_TEXT, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					for (String license : licenses) {
						int indent = 8;
						for (OrderedText line : textRenderer.wrapLines(Text.literal(license), wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, indent));
							indent = 16;
						}
					}
				}

				if (!ModScreenConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(emptyEntry);

						for (OrderedText line : textRenderer.wrapLines(VIEW_CREDITS_TEXT, wrapWidth)) {
							children().add(new MojangCreditsEntry(line));
						}
					} else if (!"java".equals(mod.getId())) {
						var credits = mod.getCredits();

						if (!credits.isEmpty()) {
							children().add(emptyEntry);

							for (OrderedText line : textRenderer.wrapLines(CREDITS_TEXT, wrapWidth)) {
								children().add(new DescriptionEntry(line));
							}

							var iterator = credits.entrySet().iterator();

							while (iterator.hasNext()) {
								int indent = 8;

								var role = iterator.next();
								var roleName = role.getKey();

								for (var line : textRenderer.wrapLines(this.creditsRoleText(roleName),
									wrapWidth - 16
								)) {
									children().add(new DescriptionEntry(line, indent));
									indent = 16;
								}

								for (var contributor : role.getValue()) {
									indent = 16;

									for (var line : textRenderer.wrapLines(Text.literal(contributor), wrapWidth - 24)) {
										children().add(new DescriptionEntry(line, indent));
										indent = 24;
									}
								}

								if (iterator.hasNext()) {
									children().add(emptyEntry);
								}
							}
						}
					}
				}
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder;
		BuiltBuffer builtBuffer;

		this.enableScissor(guiGraphics);
		super.renderEntries(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.disableScissor();

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ZERO,
			GlStateManager.DestFactor.ONE
		);
//		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShader(C_qrsmnkgy.POSITION_COLOR);

		bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.xyz(this.getX(), (this.getY() + 4), 0.0F).

			color(0, 0, 0, 0);

		bufferBuilder.xyz(this.getXEnd(), (this.getY() + 4), 0.0F).

			color(0, 0, 0, 0);

		bufferBuilder.xyz(this.getXEnd(), this.getY(), 0.0F).

			color(0, 0, 0, 255);

		bufferBuilder.xyz(this.getX(), this.getY(), 0.0F).

			color(0, 0, 0, 255);

		bufferBuilder.xyz(this.getX(), this.getYEnd(), 0.0F).

			color(0, 0, 0, 255);

		bufferBuilder.xyz(this.getXEnd(), this.getYEnd(), 0.0F).

			color(0, 0, 0, 255);

		bufferBuilder.xyz(this.getXEnd(), (this.getYEnd() - 4), 0.0F).

			color(0, 0, 0, 0);

		bufferBuilder.xyz(this.getX(), (this.getYEnd() - 4), 0.0F).

			color(0, 0, 0, 0);

		try {
			builtBuffer = bufferBuilder.end();
			BufferRenderer.drawWithShader(builtBuffer);
			builtBuffer.close();
		} catch (Exception e) {
			// Ignored
		}
		this.renderScrollBar(bufferBuilder, tessellator);

		RenderSystem.disableBlend();
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tessellator tessellator) {
		BuiltBuffer builtBuffer;
		int scrollbarStartX = this.getScrollbarPositionX();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			int p = (int) ((float) ((this.getYEnd() - this.getY()) * (this.getYEnd() - this.getY())) / (float) this.getMaxPosition());
			p = MathHelper.clamp(p, 32, this.getYEnd() - this.getY() - 8);
			int q = (int) this.getScrollAmount() * (this.getYEnd() - this.getY() - p) / maxScroll + this.getY();
			if (q < this.getY()) {
				q = this.getY();
			}

			bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			bufferBuilder.xyz(scrollbarStartX, this.getYEnd(), 0.0F).color(0, 0, 0, 255);
			bufferBuilder.xyz(scrollbarEndX, this.getYEnd(), 0.0F).color(0, 0, 0, 255);
			bufferBuilder.xyz(scrollbarEndX, this.getY(), 0.0F).color(0, 0, 0, 255);
			bufferBuilder.xyz(scrollbarStartX, this.getY(), 0.0F).color(0, 0, 0, 255);
			bufferBuilder.xyz(scrollbarStartX, q + p, 0.0F).color(128, 128, 128, 255);
			bufferBuilder.xyz(scrollbarEndX, q + p, 0.0F).color(128, 128, 128, 255);
			bufferBuilder.xyz(scrollbarEndX, q, 0.0F).color(128, 128, 128, 255);
			bufferBuilder.xyz(scrollbarStartX, q, 0.0F).color(128, 128, 128, 255);
			bufferBuilder.xyz(scrollbarStartX, q + p - 1, 0.0F).color(192, 192, 192, 255);
			bufferBuilder.xyz(scrollbarEndX - 1, q + p - 1, 0.0F).color(192, 192, 192, 255);
			bufferBuilder.xyz(scrollbarEndX - 1, q, 0.0F).color(192, 192, 192, 255);
			bufferBuilder.xyz(scrollbarStartX, q, 0.0F).color(192, 192, 192, 255);
			try {
				builtBuffer = bufferBuilder.end();
				BufferRenderer.drawWithShader(builtBuffer);
				builtBuffer.close();
			} catch (Exception e) {
				// Ignored
			}
		}
	}

	private Text creditsRoleText(String roleName) {
		// Replace spaces and dashes in role names with underscores if they exist
		// Notably Quilted Fabric API does this with FabricMC as "Upstream Owner"
		var translationKey = roleName.replaceAll("[ -]", "_").toLowerCase();

		// Add an s to the default untranslated string if it ends in r since this
		// Fixes common role names people use in English (e.g. Author -> Authors)
		var fallback = roleName.endsWith("r") ? roleName + "s" : roleName;

		return Text.translatableWithFallback("modscreen.credits.role." + translationKey, fallback)
			.append(Text.literal(":"));
	}

	protected class DescriptionEntry extends ElementListWidget.Entry<DescriptionEntry> {
		protected OrderedText text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(OrderedText text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(OrderedText text) {
			this(text, 0);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void render(
			GuiGraphics guiGraphics,
			int index,
			int y,
			int x,
			int itemWidth,
			int itemHeight,
			int mouseX,
			int mouseY,
			boolean isSelected,
			float delta
		) {
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(guiGraphics, x + indent, y);
				x += 11;
			}
			guiGraphics.drawShadowedText(textRenderer, text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return Collections.emptyList();
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(OrderedText text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				client.setScreen(new MinecraftCredits());
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		class MinecraftCredits extends CreditsAndAttributionScreen {
			public MinecraftCredits() {
				super(parent);
			}
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(OrderedText text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(OrderedText text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				client.setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getOperatingSystem().open(link);
					}
					client.setScreen(parent);
				}, link, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

}

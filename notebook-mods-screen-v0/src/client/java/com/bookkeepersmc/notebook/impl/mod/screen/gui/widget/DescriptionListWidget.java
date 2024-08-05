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
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateInfo;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries.ModListEntry;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;

public class DescriptionListWidget extends AbstractSelectionList<DescriptionListWidget.DescriptionEntry> {

	private static final Component HAS_UPDATE_TEXT = Component.translatable("modscreen.hasUpdate");
	private static final Component EXPERIMENTAL_TEXT = Component.translatable("modscreen.experimental").withStyle(ChatFormatting.GOLD);
	private static final Component DOWNLOAD_TEXT = Component.translatable("modscreen.downloadLink")
		.withStyle(ChatFormatting.BLUE)
		.withStyle(ChatFormatting.UNDERLINE);
	private static final Component CHILD_HAS_UPDATE_TEXT = Component.translatable("modscreen.childHasUpdate");
	private static final Component LINKS_TEXT = Component.translatable("modscreen.links");
	private static final Component SOURCE_TEXT = Component.translatable("modscreen.source")
		.withStyle(ChatFormatting.BLUE)
		.withStyle(ChatFormatting.UNDERLINE);
	private static final Component LICENSE_TEXT = Component.translatable("modscreen.license");
	private static final Component VIEW_CREDITS_TEXT = Component.translatable("modscreen.viewCredits")
		.withStyle(ChatFormatting.BLUE)
		.withStyle(ChatFormatting.UNDERLINE);
	private static final Component CREDITS_TEXT = Component.translatable("modscreen.credits");

	private final ModsScreen parent;
	private final Font font;
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
		this.font = client.font;
	}

	@Override
	public DescriptionEntry getSelected() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + this.getX();
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput builder) {
		Mod mod = parent.getSelectedEntry().getMod();
		builder.add(NarratedElementType.TITLE, mod.getTranslatedName() + " " + mod.getPrefixedVersion());
	}

	@Override
	public void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			if (lastSelected != null) {
				DescriptionEntry emptyEntry = new DescriptionEntry(FormattedCharSequence.EMPTY);
				int wrapWidth = getRowWidth() - 5;

				Mod mod = lastSelected.getMod();
				Component description = mod.getFormattedDescription();
				if (!description.getString().isEmpty()) {
					for (FormattedCharSequence line : font.split(description, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}
				}

				if (ModScreenConfig.UPDATE_CHECKER.getValue() && !ModScreenConfig.DISABLE_UPDATE_CHECKER.getValue()
					.contains(mod.getId())) {
					UpdateInfo updateInfo = mod.getUpdateInfo();
					if (updateInfo != null && updateInfo.isUpdateAvailable()) {
						children().add(emptyEntry);

						int index = 0;
						for (FormattedCharSequence line : font.split(HAS_UPDATE_TEXT, wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) {
								entry.setUpdateTextEntry();
							}

							children().add(entry);
							index += 1;
						}

						for (FormattedCharSequence line : font.split(EXPERIMENTAL_TEXT, wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, 8));
						}


						Component updateMessage = updateInfo.getUpdateMessage();
						String downloadLink = updateInfo.getDownloadLink();
						if (updateMessage == null) {
							updateMessage = DOWNLOAD_TEXT;
						} else {
							if (downloadLink != null) {
								updateMessage = updateMessage.copy()
									.withStyle(ChatFormatting.BLUE)
									.withStyle(ChatFormatting.UNDERLINE);
							}
						}
						for (FormattedCharSequence line : font.split(updateMessage, wrapWidth - 16)) {
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
						for (FormattedCharSequence line : font.split(CHILD_HAS_UPDATE_TEXT, wrapWidth - 11)) {
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

					for (FormattedCharSequence line : font.split(LINKS_TEXT, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (FormattedCharSequence line : font.split(SOURCE_TEXT, wrapWidth - 16)) {
							children().add(new LinkEntry(line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (FormattedCharSequence line : font.split(Component.translatable(key)
								.withStyle(ChatFormatting.BLUE)
								.withStyle(ChatFormatting.UNDERLINE),
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

					for (FormattedCharSequence line : font.split(LICENSE_TEXT, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					for (String license : licenses) {
						int indent = 8;
						for (FormattedCharSequence line : font.split(Component.literal(license), wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, indent));
							indent = 16;
						}
					}
				}

				if (!ModScreenConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(emptyEntry);

						for (FormattedCharSequence line : font.split(VIEW_CREDITS_TEXT, wrapWidth)) {
							children().add(new MojangCreditsEntry(line));
						}
					} else if (!"java".equals(mod.getId())) {
						var credits = mod.getCredits();

						if (!credits.isEmpty()) {
							children().add(emptyEntry);

							for (FormattedCharSequence line : font.split(CREDITS_TEXT, wrapWidth)) {
								children().add(new DescriptionEntry(line));
							}

							var iterator = credits.entrySet().iterator();

							while (iterator.hasNext()) {
								int indent = 8;

								var role = iterator.next();
								var roleName = role.getKey();

								for (var line : font.split(this.creditsRoleText(roleName),
									wrapWidth - 16
								)) {
									children().add(new DescriptionEntry(line, indent));
									indent = 16;
								}

								for (var contributor : role.getValue()) {
									indent = 16;

									for (var line : font.split(Component.literal(contributor), wrapWidth - 24)) {
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

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder;
		MeshData builtBuffer;

		this.enableScissor(guiGraphics);
		super.renderListItems(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.disableScissor();

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ZERO,
			GlStateManager.DestFactor.ONE
		);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.addVertex(this.getX(), (this.getY() + 4), 0.0F).

			setColor(0, 0, 0, 0);

		bufferBuilder.addVertex(this.getRight(), (this.getY() + 4), 0.0F).

			setColor(0, 0, 0, 0);

		bufferBuilder.addVertex(this.getRight(), this.getY(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getX(), this.getY(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getX(), this.getBottom(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getRight(), this.getBottom(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getRight(), (this.getBottom() - 4), 0.0F).

			setColor(0, 0, 0, 0);

		bufferBuilder.addVertex(this.getX(), (this.getBottom() - 4), 0.0F).

			setColor(0, 0, 0, 0);

		try {
			builtBuffer = bufferBuilder.buildOrThrow();
			BufferUploader.drawWithShader(builtBuffer);
			builtBuffer.close();
		} catch (Exception e) {
			// Ignored
		}
		this.renderScrollBar(bufferBuilder, tessellator);

		RenderSystem.disableBlend();
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tesselator tessellator) {
		MeshData builtBuffer;
		int scrollbarStartX = this.getScrollbarPosition();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			int p = (int) ((float) ((this.getBottom() - this.getY()) * (this.getBottom() - this.getY())) / (float) this.getMaxPosition());
			p = Mth.clamp(p, 32, this.getBottom() - this.getY() - 8);
			int q = (int) this.getScrollAmount() * (this.getBottom() - this.getY() - p) / maxScroll + this.getY();
			if (q < this.getY()) {
				q = this.getY();
			}

			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.addVertex(scrollbarStartX, this.getBottom(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarEndX, this.getBottom(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarEndX, this.getY(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarStartX, this.getY(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarStartX, q + p, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarEndX, q + p, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarEndX, q, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarStartX, q, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarStartX, q + p - 1, 0.0F).setColor(192, 192, 192, 255);
			bufferBuilder.addVertex(scrollbarEndX - 1, q + p - 1, 0.0F).setColor(192, 192, 192, 255);
			bufferBuilder.addVertex(scrollbarEndX - 1, q, 0.0F).setColor(192, 192, 192, 255);
			bufferBuilder.addVertex(scrollbarStartX, q, 0.0F).setColor(192, 192, 192, 255);
			try {
				builtBuffer = bufferBuilder.buildOrThrow();
				BufferUploader.drawWithShader(builtBuffer);
				builtBuffer.close();
			} catch (Exception e) {
				// Ignored
			}
		}
	}

	private Component creditsRoleText(String roleName) {
		// Replace spaces and dashes in role names with underscores if they exist
		// Notably Quilted Fabric API does this with FabricMC as "Upstream Owner"
		var translationKey = roleName.replaceAll("[ -]", "_").toLowerCase();

		// Add an s to the default untranslated string if it ends in r since this
		// Fixes common role names people use in English (e.g. Author -> Authors)
		var fallback = roleName.endsWith("r") ? roleName + "s" : roleName;

		return Component.translatableWithFallback("modscreen.credits.role." + translationKey, fallback)
			.append(Component.literal(":"));
	}

	protected class DescriptionEntry extends ContainerObjectSelectionList.Entry<DescriptionEntry> {
		protected FormattedCharSequence text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(FormattedCharSequence text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(FormattedCharSequence text) {
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
			guiGraphics.drawString(font, text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return Collections.emptyList();
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(FormattedCharSequence text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new MinecraftCredits());
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

		public LinkEntry(FormattedCharSequence text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(FormattedCharSequence text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getPlatform().openUri(link);
					}
					minecraft.setScreen(parent);
				}, link, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

}

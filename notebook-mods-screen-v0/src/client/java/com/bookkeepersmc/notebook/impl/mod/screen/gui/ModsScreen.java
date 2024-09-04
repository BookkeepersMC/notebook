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
package com.bookkeepersmc.notebook.impl.mod.screen.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.UrlConstants;
import net.minecraft.util.Util;

import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfigManager;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.DescriptionListWidget;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.LegacyTexturedButtonWidget;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.ModListWidget;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries.ModListEntry;
import com.bookkeepersmc.notebook.impl.mod.screen.util.DrawingUtil;
import com.bookkeepersmc.notebook.impl.mod.screen.util.ModScreenTexts;
import com.bookkeepersmc.notebook.impl.mod.screen.util.TranslationUtil;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.ModBadgeRenderer;

public class ModsScreen extends Screen {
	private static final Identifier FILTERS_BUTTON_LOCATION = Identifier.of(NotebookModScreen.MOD_ID,
			"textures/gui/filters_button.png"
	);
	private static final Identifier CONFIGURE_BUTTON_LOCATION = Identifier.of(NotebookModScreen.MOD_ID,
			"textures/gui/configure_button.png"
	);

	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | ModsScreen");
	private final Screen previousScreen;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private double scrollPercent = 0;
	private boolean keepFilterOptionsShown = false;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	private TextFieldWidget searchBox;
	private @Nullable ClickableWidget filtersButton;
	private ClickableWidget sortingButton;
	private ClickableWidget librariesButton;
	private ModListWidget modList;
	private @Nullable ClickableWidget configureButton;
	private ClickableWidget websiteButton;
	private ClickableWidget issuesButton;
	private DescriptionListWidget descriptionListWidget;
	private ClickableWidget modsFolderButton;
	private ClickableWidget doneButton;

	public final Map<String, Boolean> modHasConfigScreen = new HashMap<>();
	public final Map<String, Throwable> modScreenErrors = new HashMap<>();

	private static final Text SEND_FEEDBACK_TEXT = Text.translatable("menu.sendFeedback");
	private static final Text REPORT_BUGS_TEXT = Text.translatable("menu.reportBugs");

	public ModsScreen(Screen previousScreen) {
		super(ModScreenTexts.TITLE);
		this.previousScreen = previousScreen;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (modList.isMouseOver(mouseX, mouseY)) {
			return this.modList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		if (descriptionListWidget.isMouseOver(mouseX, mouseY)) {
			return this.descriptionListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		return false;
	}

	@Override
	protected void init() {
		for (Mod mod : NotebookModScreen.MODS.values()) {
			String id = mod.getId();
			if (!modHasConfigScreen.containsKey(id)) {
				try {
					Screen configScreen = NotebookModScreen.getConfigScreen(id, this);
					modHasConfigScreen.put(id, configScreen != null);
				} catch (java.lang.NoClassDefFoundError e) {
					LOGGER.warn(
							"The '" + id + "' mod config screen is not available because " + e.getLocalizedMessage() +
									" is missing.");
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				} catch (Throwable e) {
					LOGGER.error("Error from mod '" + id + "'", e);
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				}
			}
		}

		int paneY = ModScreenConfig.CONFIG_MODE.getValue() ? 48 : 48 + 19;
		this.paneWidth = this.width / 2 - 8;
		this.rightPaneX = this.width - this.paneWidth;

		// Mod list (initialized early for updateFiltersX)
		this.modList = new ModListWidget(this.client,
				this.paneWidth,
				this.height - paneY - 36,
				paneY,
				ModScreenConfig.COMPACT_LIST.getValue() ? 23 : 36,
				this.modList,
				this
		);
		this.modList.setX(0);

		// Search box
		int filtersButtonSize = (ModScreenConfig.CONFIG_MODE.getValue() ? 0 : 22);
		int searchWidthMax = this.paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModScreenConfig.CONFIG_MODE.getValue() ? Math.min(200, searchWidthMax) : searchWidthMax;

		this.searchBoxX = this.paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;

		this.searchBox = new TextFieldWidget(this.textRenderer,
				this.searchBoxX,
				22,
				searchBoxWidth,
				20,
				this.searchBox,
				ModScreenTexts.SEARCH
		);
		this.searchBox.setChangedListener(text -> {
			this.modList.filter(text, false);
		});

		// Filters button
		Text sortingText = ModScreenConfig.SORTING.getButtonText();
		Text librariesText = ModScreenConfig.SHOW_LIBRARIES.getButtonText();

		int sortingWidth = textRenderer.getWidth(sortingText) + 20;
		int librariesWidth = textRenderer.getWidth(librariesText) + 20;

		this.filtersWidth = librariesWidth + sortingWidth + 2;
		this.searchRowWidth = this.searchBoxX + searchBoxWidth + 22;

		this.updateFiltersX();

		if (!ModScreenConfig.CONFIG_MODE.getValue()) {
			this.filtersButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(ModScreenTexts.TOGGLE_FILTER_OPTIONS,
							button -> {
								this.setFilterOptionsShown(!this.filterOptionsShown);
							}
					)
					.position(this.paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22)
					.size(20, 20)
					.uv(0, 0, 20)
					.texture(FILTERS_BUTTON_LOCATION, 32, 64)
					.build();

			this.filtersButton.setTooltip(Tooltip.create(ModScreenTexts.TOGGLE_FILTER_OPTIONS));
		}

		// Sorting button
		this.sortingButton = ButtonWidget.builder(sortingText, button -> {
			ModScreenConfig.SORTING.cycleValue();
			ModScreenConfigManager.save();
			modList.reloadFilters();
			button.setMessage(ModScreenConfig.SORTING.getButtonText());
		}).position(this.filtersX, 45).size(sortingWidth, 20).build();

		// Show libraries button
		this.librariesButton = ButtonWidget.builder(librariesText, button -> {
			ModScreenConfig.SHOW_LIBRARIES.toggleValue();
			ModScreenConfigManager.save();
			modList.reloadFilters();
			button.setMessage(ModScreenConfig.SHOW_LIBRARIES.getButtonText());
		}).position(this.filtersX + sortingWidth + 2, 45).size(librariesWidth, 20).build();

		// Configure button
		if (!ModScreenConfig.HIDE_CONFIG_BUTTONS.getValue()) {
			this.configureButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonTexts.EMPTY, button -> {
						final String id = Objects.requireNonNull(selected).getMod().getId();
						if (modHasConfigScreen.get(id)) {
							Screen configScreen = NotebookModScreen.getConfigScreen(id, this);
							client.setScreen(configScreen);
						} else {
							button.active = false;
						}
					})
					.position(width - 24, RIGHT_PANE_Y)
					.size(20, 20)
					.uv(0, 0, 20)
					.texture(CONFIGURE_BUTTON_LOCATION, 32, 64)
					.build();
		}

		// Website button
		int urlButtonWidths = this.paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);

		this.websiteButton = ButtonWidget.builder(ModScreenTexts.WEBSITE, button -> {
					final Mod mod = Objects.requireNonNull(selected).getMod();
					boolean isMinecraft = selected.getMod().getId().equals("minecraft");

					if (isMinecraft) {
						var url = SharedConstants.getGameVersion().isStable() ? UrlConstants.JAVA_FEEDBACK : UrlConstants.SNAPSHOT_FEEDBACK;
						ConfirmLinkScreen.openConfirmLink(this, url, true);
					} else {
						var url = mod.getWebsite();
						ConfirmLinkScreen.openConfirmLink(this, url, false);
					}
				})
				.position(this.rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36)
				.size(Math.min(urlButtonWidths, 200), 20)
				.build();

		// Issues button
		this.issuesButton = ButtonWidget.builder(ModScreenTexts.ISSUES, button -> {
					final Mod mod = Objects.requireNonNull(selected).getMod();
					boolean isMinecraft = selected.getMod().getId().equals("minecraft");

					if (isMinecraft) {
						ConfirmLinkScreen.openConfirmLink(this, UrlConstants.SNAPSHOT_BUGS, true);
					} else {
						var url = mod.getIssueTracker();
						ConfirmLinkScreen.openConfirmLink(this, url, false);
					}
				})
				.position(this.rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2),
						RIGHT_PANE_Y + 36
				)
				.size(Math.min(urlButtonWidths, 200), 20)
				.build();

		// Description list
		this.descriptionListWidget = new DescriptionListWidget(this.client,
				this.paneWidth,
				this.height - RIGHT_PANE_Y - 96,
				RIGHT_PANE_Y + 60,
				textRenderer.fontHeight + 1,
				this
		);
		this.descriptionListWidget.setX(this.rightPaneX);

		// Mods folder button
		this.modsFolderButton = ButtonWidget.builder(ModScreenTexts.MODS_FOLDER, button -> {
			Util.getOperatingSystem().open(NotebookLoader.getInstance().getGameDir().resolve("mods").toUri());
		}).position(this.width / 2 - 154, this.height - 28).size(150, 20).build();

		// Done button
		this.doneButton = ButtonWidget.builder(CommonTexts.DONE, button -> {
			client.setScreen(previousScreen);
		}).position(this.width / 2 + 4, this.height - 28).size(150, 20).build();

		// Initialize data
		modList.reloadFilters();
		this.setFilterOptionsShown(this.keepFilterOptionsShown && this.filterOptionsShown);

		// Add children
		this.addSelectableElement(this.searchBox);
		this.setInitialFocus(this.searchBox);

		if (this.filtersButton != null) {
			this.addDrawable(this.filtersButton);
		}

		this.addDrawableSelectableElement(this.sortingButton);
		this.addDrawableSelectableElement(this.librariesButton);
		this.addSelectableElement(this.modList);

		if (this.configureButton != null) {
			this.addDrawableSelectableElement(this.configureButton);
		}

		this.addDrawableSelectableElement(this.websiteButton);
		this.addDrawableSelectableElement(this.issuesButton);
		this.addSelectableElement(this.descriptionListWidget);
		this.addDrawableSelectableElement(this.modsFolderButton);
		this.addDrawableSelectableElement(this.doneButton);

		this.init = true;
		this.keepFilterOptionsShown = true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers) ||
				this.searchBox.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int keyCode) {
		return this.searchBox.charTyped(chr, keyCode);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.render(guiGraphics, mouseX, mouseY, delta);
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(guiGraphics, mouseX, mouseY, delta);
		}
		this.modList.render(guiGraphics, mouseX, mouseY, delta);
		this.searchBox.render(guiGraphics, mouseX, mouseY, delta);
		RenderSystem.disableBlend();
		guiGraphics.drawCenteredShadowedText(this.textRenderer, this.title, this.modList.getWidth() / 2, 8, 16777215);
		if (!ModScreenConfig.DISABLE_DRAG_AND_DROP.getValue()) {
			guiGraphics.drawCenteredShadowedText(this.textRenderer,
					ModScreenTexts.DROP_INFO_LINE_1,
					this.width - this.modList.getWidth() / 2,
					RIGHT_PANE_Y / 2 - client.textRenderer.fontHeight - 1,
					Formatting.GRAY.getColorValue()
			);
			guiGraphics.drawCenteredShadowedText(this.textRenderer,
					ModScreenTexts.DROP_INFO_LINE_2,
					this.width - this.modList.getWidth() / 2,
					RIGHT_PANE_Y / 2 + 1,
					Formatting.GRAY.getColorValue()
			);
		}
		if (!ModScreenConfig.CONFIG_MODE.getValue()) {
			Text fullModCount = this.computeModCountText(true);
			if (!ModScreenConfig.CONFIG_MODE.getValue() && this.updateFiltersX()) {
				if (this.filterOptionsShown) {
					if (!ModScreenConfig.SHOW_LIBRARIES.getValue() ||
							textRenderer.getWidth(fullModCount) <= this.filtersX - 5) {
						guiGraphics.drawText(textRenderer,
								fullModCount.asOrderedText(),
								this.searchBoxX,
								52,
								0xFFFFFF,
								true
						);
					} else {
						guiGraphics.drawText(textRenderer,
								computeModCountText(false).asOrderedText(),
								this.searchBoxX,
								46,
								0xFFFFFF,
								true
						);
						guiGraphics.drawText(textRenderer,
								computeLibraryCountText().asOrderedText(),
								this.searchBoxX,
								57,
								0xFFFFFF,
								true
						);
					}
				} else {
					if (!ModScreenConfig.SHOW_LIBRARIES.getValue() ||
							textRenderer.getWidth(fullModCount) <= modList.getWidth() - 5) {
						guiGraphics.drawText(textRenderer,
								fullModCount.asOrderedText(),
								this.searchBoxX,
								52,
								0xFFFFFF,
								true
						);
					} else {
						guiGraphics.drawText(textRenderer,
								computeModCountText(false).asOrderedText(),
								this.searchBoxX,
								46,
								0xFFFFFF,
								true
						);
						guiGraphics.drawText(textRenderer,
								computeLibraryCountText().asOrderedText(),
								this.searchBoxX,
								57,
								0xFFFFFF,
								true
						);
					}
				}
			}
		}
		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = this.rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, guiGraphics, x, RIGHT_PANE_Y, 32, 32);
			}
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			guiGraphics.method_25290(RenderLayer::getGuiTextured, this.selected.getIconTexture(), x, RIGHT_PANE_Y, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			int lineSpacing = textRenderer.fontHeight + 1;
			int imageOffset = 36;
			Text name = Text.literal(mod.getTranslatedName());
			StringVisitable trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (textRenderer.getWidth(name) > maxNameWidth) {
				StringVisitable ellipsis = StringVisitable.plain("...");
				trimmedName = StringVisitable.concat(textRenderer.trimToWidth(name,
						maxNameWidth - textRenderer.getWidth(ellipsis)
				), ellipsis);
			}
			guiGraphics.drawText(textRenderer,
					Language.getInstance().reorder(trimmedName),
					x + imageOffset,
					RIGHT_PANE_Y + 1,
					0xFFFFFF,
					true
			);
			if (mouseX > x + imageOffset && mouseY > RIGHT_PANE_Y + 1 &&
					mouseY < RIGHT_PANE_Y + 1 + textRenderer.fontHeight &&
					mouseX < x + imageOffset + textRenderer.getWidth(trimmedName)) {
				this.setDeferredTooltip(ModScreenTexts.modIdTooltip(mod.getId()));
			}
			if (this.init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(
						x + imageOffset + this.client.textRenderer.getWidth(trimmedName) + 2,
						RIGHT_PANE_Y,
						width - 28,
						selectedEntry.mod,
						this
				);
				this.init = false;
			}
			if (!ModScreenConfig.HIDE_BADGES.getValue()) {
				modBadgeRenderer.draw(guiGraphics, mouseX, mouseY);
			}
			if (mod.isReal()) {
				guiGraphics.drawText(textRenderer,
						mod.getPrefixedVersion(),
						x + imageOffset,
						RIGHT_PANE_Y + 2 + lineSpacing,
						0x808080,
						true
				);
			}
			String authors;
			List<String> names = mod.getAuthors();

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				DrawingUtil.drawWrappedString(guiGraphics,
						I18n.translate("modscreen.authorPrefix", authors),
						x + imageOffset,
						RIGHT_PANE_Y + 2 + lineSpacing * 2,
						this.paneWidth - imageOffset - 4,
						1,
						0x808080
				);
			}
		}
	}

	private Text computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(NotebookModScreen.ROOT_MODS.values()
				.stream()
				.filter(mod -> !mod.isHidden() && !mod.getBadges().contains(Mod.Badge.LIBRARY))
				.map(Mod::getId)
				.collect(Collectors.toSet()));

		if (includeLibs && ModScreenConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(NotebookModScreen.ROOT_MODS.values()
					.stream()
					.filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY))
					.map(Mod::getId)
					.collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modscreen.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modscreen.showingMods", rootMods);
		}
	}

	private Text computeLibraryCountText() {
		if (ModScreenConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(NotebookModScreen.ROOT_MODS.values()
					.stream()
					.filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY))
					.map(Mod::getId)
					.collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modscreen.showingLibraries", rootLibs);
		} else {
			return Text.literal(null);
		}
	}

	private int[] formatModCount(Set<String> set) {
		int visible = this.modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total) {
			return new int[]{ total };
		}
		return new int[]{ visible, total };
	}

	@Override
	public void closeScreen() {
		this.modList.close();
		this.client.setScreen(this.previousScreen);
	}

	private void setFilterOptionsShown(boolean filterOptionsShown) {
		this.filterOptionsShown = filterOptionsShown;

		this.sortingButton.visible = filterOptionsShown;
		this.librariesButton.visible = filterOptionsShown;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
			String modId = selected.getMod().getId();

			if (this.configureButton != null) {

				this.configureButton.active = modHasConfigScreen.get(modId);
				this.configureButton.visible =
						selected != null && modHasConfigScreen.get(modId) || modScreenErrors.containsKey(modId);

				if (modScreenErrors.containsKey(modId)) {
					Throwable e = modScreenErrors.get(modId);
					this.configureButton.setTooltip(Tooltip.create(ModScreenTexts.configureError(modId, e)));
				} else {
					this.configureButton.setTooltip(Tooltip.create(ModScreenTexts.CONFIGURE));
				}
			}

			var isMinecraft = modId.equals("minecraft");

			if (isMinecraft) {
				this.websiteButton.setMessage(SEND_FEEDBACK_TEXT);
				this.issuesButton.setMessage(REPORT_BUGS_TEXT);
			} else {
				this.websiteButton.setMessage(ModScreenTexts.WEBSITE);
				this.issuesButton.setMessage(ModScreenTexts.ISSUES);
			}

			this.websiteButton.visible = true;
			this.websiteButton.active = isMinecraft || selected.getMod().getWebsite() != null;

			this.issuesButton.visible = true;
			this.issuesButton.active = isMinecraft || selected.getMod().getIssueTracker() != null;
		}
	}

	public double getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent(double scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return this.searchBox.getText();
	}

	private boolean updateFiltersX() {
		if ((this.filtersWidth + textRenderer.getWidth(this.computeModCountText(true)) + 20) >= this.searchRowWidth &&
				((this.filtersWidth + textRenderer.getWidth(computeModCountText(false)) + 20) >= this.searchRowWidth ||
						(this.filtersWidth + textRenderer.getWidth(this.computeLibraryCountText()) + 20) >= this.searchRowWidth
				)) {
			this.filtersX = this.paneWidth / 2 - this.filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			this.filtersX = this.searchRowWidth - this.filtersWidth + 1;
			return true;
		}
	}

	@Override
	public void filesDragged(List<Path> paths) {
		Path modsDirectory = NotebookLoader.getInstance().getGameDir().resolve("mods");

		// Filter out none mods
		List<Path> mods = paths.stream().filter(ModsScreen::isNotebookMod).collect(Collectors.toList());

		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));

		this.client.setScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					try {
						Files.copy(path, modsDirectory.resolve(path.getFileName()));
					} catch (IOException e) {
						LOGGER.warn("Failed to copy mod from {} to {}",
								path,
								modsDirectory.resolve(path.getFileName())
						);
						SystemToast.addPackCopyFailureToast(client, path.toString());
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					SystemToast.add(client.method_1566(),
							SystemToast.Id.PERIODIC_NOTIFICATION,
							ModScreenTexts.DROP_SUCCESSFUL_LINE_1,
							ModScreenTexts.DROP_SUCCESSFUL_LINE_1
					);
				}
			}
			this.client.setScreen(this);
		}, ModScreenTexts.DROP_CONFIRM, Text.literal(modList)));
	}

	private static boolean isNotebookMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("notebook.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}

	public Map<String, Boolean> getModHasConfigScreen() {
		return this.modHasConfigScreen;
	}
}

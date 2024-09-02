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

import java.util.*;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferRenderer;
import com.mojang.blaze3d.vertex.BuiltBuffer;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_qrsmnkgy;
import net.minecraft.util.math.MathHelper;

import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries.ChildEntry;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries.IndependentEntry;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries.ModListEntry;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.entries.ParentEntry;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.ModSearch;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.notebook.IconHandler;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> implements AutoCloseable {
	public static final boolean DEBUG = Boolean.getBoolean("modscreen.debug");
	private final ModsScreen parent;
	private List<Mod> mods = null;
	private final Set<Mod> addedMods = new HashSet<>();
	private String selectedModId = null;
	private boolean scrolling;
	private final IconHandler iconHandler = new IconHandler();

	public ModListWidget(
		Minecraft client,
		int width,
		int height,
		int y,
		int itemHeight,
		ModListWidget list,
		ModsScreen parent
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		if (list != null) {
			this.mods = list.mods;
		}
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.getMaxPosition() - (this.getYEnd() - this.getY() - 4));
		if (denominator <= 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(getScrollAmount() / Math.max(
				0,
				this.getMaxPosition() - (this.getYEnd() - this.getY() - 4)
			));
		}
	}

	@Override
	public boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
		if (entry != null) {
			Mod mod = entry.getMod();
			this.client.getChatNarratorManager()
				.narrate(Text.translatable("narrator.select", mod.getTranslatedName()).getString());
		}
	}

	@Override
	public void setSelected(ModListEntry entry) {
		super.setSelected(entry);
		selectedModId = entry.getMod().getId();
		parent.updateSelectedEntry(getSelectedOrNull());
	}

	@Override
	protected boolean isSelectedEntry(int index) {
		ModListEntry selected = getSelectedOrNull();
		return selected != null && selected.getMod().getId().equals(getEntry(index).getMod().getId());
	}

	@Override
	public int addEntry(ModListEntry entry) {
		if (addedMods.contains(entry.mod)) {
			return 0;
		}
		addedMods.add(entry.mod);
		int i = super.addEntry(entry);
		if (entry.getMod().getId().equals(selectedModId)) {
			setSelected(entry);
		}
		return i;
	}

	@Override
	protected boolean removeEntry(ModListEntry entry) {
		addedMods.remove(entry.mod);
		return super.removeEntry(entry);
	}

	@Override
	protected ModListEntry remove(int index) {
		addedMods.remove(getEntry(index).mod);
		return super.remove(index);
	}

	public void reloadFilters() {
		filter(parent.getSearchInput(), true, false);
	}


	public void filter(String searchTerm, boolean refresh) {
		filter(searchTerm, refresh, true);
	}

	private boolean hasVisibleChildMods(Mod parent) {
		List<Mod> children = NotebookModScreen.PARENT_MAP.get(parent);
		boolean hideLibraries = !ModScreenConfig.SHOW_LIBRARIES.getValue();

		return !children.stream()
			.allMatch(child -> child.isHidden() || hideLibraries && child.getBadges().contains(Mod.Badge.LIBRARY));
	}

	private void filter(String searchTerm, boolean refresh, boolean search) {
		this.clearEntries();
		addedMods.clear();
		Collection<Mod> mods = NotebookModScreen.MODS.values().stream().filter(mod -> {
			if (ModScreenConfig.CONFIG_MODE.getValue()) {
				Map<String, Boolean> modHasConfigScreen = parent.getModHasConfigScreen();
				var hasConfig = modHasConfigScreen.get(mod.getId());
				if (!hasConfig) {
					return false;
				}
			}

			return !mod.isHidden();
		}).collect(Collectors.toSet());

		if (DEBUG) {
			mods = new ArrayList<>(mods);
			//			mods.addAll(TestModContainer.getTestModContainers());
		}

		if (this.mods == null || refresh) {
			this.mods = new ArrayList<>();
			this.mods.addAll(mods);
			this.mods.sort(ModScreenConfig.SORTING.getValue().getComparator());
		}

		List<Mod> matched = ModSearch.search(parent, searchTerm, this.mods);

		for (Mod mod : matched) {
			String modId = mod.getId();

			//Hide parent lib mods when the config is set to hide
			if (mod.getBadges().contains(Mod.Badge.LIBRARY) && !ModScreenConfig.SHOW_LIBRARIES.getValue()) {
				continue;
			}

			if (!NotebookModScreen.PARENT_MAP.values().contains(mod)) {
				if (NotebookModScreen.PARENT_MAP.keySet().contains(mod) && hasVisibleChildMods(mod)) {
					//Add parent mods when not searching
					List<Mod> children = NotebookModScreen.PARENT_MAP.get(mod);
					children.sort(ModScreenConfig.SORTING.getValue().getComparator());
					ParentEntry parent = new ParentEntry(mod, children, this);
					this.addEntry(parent);
					//Add children if they are meant to be shown
					if (this.parent.showModChildren.contains(modId)) {
						List<Mod> validChildren = ModSearch.search(this.parent, searchTerm, children);
						for (Mod child : validChildren) {
							this.addEntry(new ChildEntry(child,
								parent,
								this,
								validChildren.indexOf(child) == validChildren.size() - 1
							));
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(mod, this));
				}
			}
		}

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelectedOrNull() != null && getSelectedOrNull().getMod() != parent.getSelectedEntry()
			.getMod()) {
			for (ModListEntry entry : children()) {
				if (entry.getMod().equals(parent.getSelectedEntry().getMod())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelectedOrNull() == null && !children().isEmpty() && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (getScrollAmount() > Math.max(0, this.getMaxPosition() - (this.getYEnd() - this.getY() - 4))) {
			setScrollAmount(Math.max(0, this.getMaxPosition() - (this.getYEnd() - this.getY() - 4)));
		}
	}


	@Override
	protected void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		int entryCount = this.getEntryCount();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer;

		for (int index = 0; index < entryCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.getY() && entryTop <= this.getYEnd()) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.isSelectedEntry(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = this.getRowLeft() + rowWidth + 2;
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					RenderSystem.setShader(C_qrsmnkgy.POSITION);
					RenderSystem.setShaderColor(float_2, float_2, float_2, 1.0F);
					Matrix4f matrix = guiGraphics.getMatrices().peek().getModel();
					BuiltBuffer builtBuffer;
					buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
					buffer.xyz(matrix, entryLeft, entryTop + entryHeight + 2, 0.0F);
					buffer.xyz(matrix, selectionRight, entryTop + entryHeight + 2, 0.0F);
					buffer.xyz(matrix, selectionRight, entryTop - 2, 0.0F);
					buffer.xyz(matrix, entryLeft, entryTop - 2, 0.0F);
					try {
						builtBuffer = buffer.end();
						BufferRenderer.drawWithShader(builtBuffer);
						builtBuffer.close();
					} catch (Exception e) {
						// Ignored
					}
					RenderSystem.setShader(C_qrsmnkgy.POSITION);
					RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
					buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
					buffer.xyz(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F);
					buffer.xyz(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F);
					buffer.xyz(matrix, selectionRight - 1, entryTop - 1, 0.0F);
					buffer.xyz(matrix, entryLeft + 1, entryTop - 1, 0.0F);
					try {
						builtBuffer = buffer.end();
						BufferRenderer.drawWithShader(builtBuffer);
						builtBuffer.close();
					} catch (Exception e) {
						// Ignored
					}
				}

				entryLeft = this.getRowLeft();
				entry.render(guiGraphics,
					index,
					entryTop,
					entryLeft,
					rowWidth,
					entryHeight,
					mouseX,
					mouseY,
					this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry),
					delta
				);
			}
		}
	}

	public void ensureVisible(ModListEntry entry) {
		super.ensureVisible(entry);
	}

	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPositionX() && double_1 < (double) (this.getScrollbarPositionX() + 6);
	}

	@Override
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		this.updateScrollingState(double_1, double_2, int_1);
		if (!this.isMouseOver(double_1, double_2)) {
			return false;
		} else {
			ModListEntry entry = this.getEntryAtPos(double_1, double_2);
			if (entry != null) {
				if (entry.mouseClicked(double_1, double_2, int_1)) {
					this.setFocusedChild(entry);
					this.setDragging(true);
					return true;
				}
			} else if (int_1 == 0 && this.clickedHeader((int) (double_1 - (double) (this.getX() + this.width / 2 - this.getRowWidth() / 2)),
				(int) (double_2 - (double) this.getY()) + (int) this.getScrollAmount() - 4
			)) {
				return true;
			}

			return this.scrolling;
		}
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		if (getSelectedOrNull() != null) {
			return getSelectedOrNull().keyPressed(keyCode, scanCode, modifiers);
		}
		return false;
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.getY()) - this.headerHeight + (int) this.getScrollAmount() - 4;
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarPositionX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getEntryCount() ?
			this.children().get(index) :
			null;
	}

	@Override
	protected int getScrollbarPositionX() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxPosition() - (this.getYEnd() - this.getY() - 4)) > 0 ? 18 : 12);
	}

	@Override
	public int getRowLeft() {
		return this.getX() + 6;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.getY();
	}

	public ModsScreen getParent() {
		return parent;
	}

	@Override
	protected int getMaxPosition() {
		return super.getMaxPosition() + 4;
	}

	public int getDisplayedCountFor(Set<String> set) {
		int count = 0;
		for (ModListEntry c : children()) {
			if (set.contains(c.getMod().getId())) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void close() {
		iconHandler.close();
	}

	public IconHandler getFabricIconHandler() {
		return iconHandler;
	}
}

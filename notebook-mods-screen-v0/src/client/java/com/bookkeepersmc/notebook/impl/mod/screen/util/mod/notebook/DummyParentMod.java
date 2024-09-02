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
package com.bookkeepersmc.notebook.impl.mod.screen.util.mod.notebook;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.texture.NativeImageBackedTexture;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateChecker;
import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateInfo;
import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;

public class DummyParentMod implements Mod {
	private final String id;
	private final NotebookMod host;
	private boolean childHasUpdate;

	public DummyParentMod(NotebookMod host, String id) {
		this.host = host;
		this.id = id;
	}

	@Override
	public @NotNull String getId() {
		return id;
	}

	@Override
	public @NotNull String getName() {
		NotebookMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if (parentData != null) {
			return parentData.getName().orElse("");
		}
		if (id.equals("fabric-api")) {
			return "Fabric API";
		}
		return id;
	}

	@Override
	public @NotNull NativeImageBackedTexture getIcon(IconHandler iconHandler, int i) {
		String iconSourceId = host.getId();
		NotebookMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		String iconPath = null;
		if (parentData != null) {
			iconPath = parentData.getIcon().orElse(null);
		}
		if ("inherit".equals(iconPath)) {
			return host.getIcon(iconHandler, i);
		}
		if (iconPath == null) {
			iconSourceId = NotebookModScreen.MOD_ID;
			if (id.equals("notebook-api")) {
				iconPath = "assets/" + NotebookModScreen.MOD_ID + "/fabric.png";
			} else {
				iconPath = "assets/" + NotebookModScreen.MOD_ID + "/unknown_parent.png";
			}
		}
		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = NotebookLoader.getInstance()
			.getModContainer(iconSourceId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		return Objects.requireNonNull(
			iconHandler.createIcon(iconSource, iconPath),
			"Mod icon for " + getId() + " is null somehow (should be filled with default in this case)"
		);
	}

	@Override
	public @NotNull String getDescription() {
		NotebookMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if (parentData != null) {
			return parentData.getDescription().orElse("");
		}
		return "";
	}

	@Override
	public @NotNull String getVersion() {
		return "";
	}

	@Override
	public @NotNull String getPrefixedVersion() {
		return "";
	}

	@Override
	public @NotNull List<String> getAuthors() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull Map<String, Collection<String>> getContributors() {
		return Map.of();
	}

	@Override
	public @NotNull SortedMap<String, Set<String>> getCredits() {
		return new TreeMap<>();
	}

	@Override
	public @NotNull Set<Badge> getBadges() {
		NotebookMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if (parentData != null) {
			return parentData.getBadges();
		}
		var badges = new HashSet<Badge>();
		if (id.equals("fabric-api")) {
			badges.add(Badge.LIBRARY);
		}

		boolean modpackChildren = true;
		for (Mod mod : NotebookModScreen.PARENT_MAP.get(this)) {
			if (!mod.getBadges().contains(Badge.MODPACK)) {
				modpackChildren = false;
			}
		}
		if (modpackChildren) {
			badges.add(Badge.MODPACK);
		}

		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		return null;
	}

	@Override
	public @Nullable String getIssueTracker() {
		return null;
	}

	@Override
	public @Nullable String getSource() {
		return null;
	}

	@Override
	public @Nullable String getParent() {
		return null;
	}

	@Override
	public @NotNull Set<String> getLicense() {
		return new HashSet<>();
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return new HashMap<>();
	}

	@Override
	public boolean isReal() {
		return false;
	}

	@Override
	public boolean allowsUpdateChecks() {
		return false;
	}

	@Override
	public @Nullable UpdateChecker getUpdateChecker() {
		return null;
	}

	@Override
	public void setUpdateChecker(@Nullable UpdateChecker updateChecker) {

	}

	@Override
	public @Nullable UpdateInfo getUpdateInfo() {
		return null;
	}

	@Override
	public void setUpdateInfo(@Nullable UpdateInfo updateInfo) {

	}

	@Override
	public boolean getChildHasUpdate() {
		return childHasUpdate;
	}

	@Override
	public void setChildHasUpdate() {
		this.childHasUpdate = true;
	}

	@Override
	public boolean isHidden() {
		return ModScreenConfig.HIDDEN_MODS.getValue().contains(this.getId());
	}
}

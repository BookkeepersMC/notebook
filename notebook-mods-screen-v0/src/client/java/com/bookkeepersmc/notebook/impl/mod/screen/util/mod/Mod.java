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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateChecker;
import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateInfo;
import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.notebook.IconHandler;

public interface Mod {
	@NotNull String getId();

	@NotNull String getName();

	@NotNull
	default String getTranslatedName() {
		String translationKey = "modscreen.nameTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModScreenConfig.TRANSLATE_NAMES.getValue()) && I18n.exists(
			translationKey)) {
			return I18n.get(translationKey);
		}
		return getName();
	}

	@NotNull
	DynamicTexture getIcon(IconHandler iconHandler, int i);

	@NotNull
	default String getSummary() {
		return getTranslatedSummary();
	}

	@NotNull
	default String getTranslatedSummary() {
		String translationKey = "modscreen.summaryTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModScreenConfig.TRANSLATE_DESCRIPTIONS.getValue()) && I18n.exists(
			translationKey)) {
			return I18n.get(translationKey);
		}
		return getTranslatedDescription();
	}

	@NotNull String getDescription();

	@NotNull
	default String getTranslatedDescription() {
		String translatableDescriptionKey = "modscreen.descriptionTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModScreenConfig.TRANSLATE_DESCRIPTIONS.getValue()) && I18n.exists(
			translatableDescriptionKey)) {
			return I18n.get(translatableDescriptionKey);
		}
		return getDescription();
	}

	default Component getFormattedDescription() {
		return Component.literal(getTranslatedDescription());
	}

	@NotNull String getVersion();

	@NotNull String getPrefixedVersion();

	@NotNull List<String> getAuthors();

	/**
	 * @return a mapping of contributors to their roles.
	 */
	@NotNull Map<String, Collection<String>> getContributors();

	/**
	 * @return a mapping of roles to each contributor with that role.
	 */
	@NotNull SortedMap<String, Set<String>> getCredits();

	@NotNull Set<Badge> getBadges();

	@Nullable String getWebsite();

	@Nullable String getIssueTracker();

	@Nullable String getSource();

	@Nullable String getParent();

	@NotNull Set<String> getLicense();

	@NotNull Map<String, String> getLinks();

	boolean isReal();

	boolean allowsUpdateChecks();

	@Nullable UpdateChecker getUpdateChecker();

	void setUpdateChecker(@Nullable UpdateChecker updateChecker);

	@Nullable UpdateInfo getUpdateInfo();

	void setUpdateInfo(@Nullable UpdateInfo updateInfo);

	default boolean hasUpdate() {
		UpdateInfo updateInfo = getUpdateInfo();
		if (updateInfo == null) {
			return false;
		}

		return updateInfo.isUpdateAvailable() && updateInfo.getUpdateChannel()
			.compareTo(ModScreenConfig.UPDATE_CHANNEL.getValue()) >= 0;
	}

	default @Nullable String getSha512Hash() throws IOException {
		return null;
	}

	void setChildHasUpdate();

	boolean getChildHasUpdate();

	boolean isHidden();

	enum Badge {
		LIBRARY("modscreen.badge.library", 0xff107454, 0xff093929, "library"), CLIENT("modscreen.badge.clientsideOnly",
			0xff2b4b7c,
			0xff0e2a55,
			null
		), DEPRECATED("modscreen.badge.deprecated", 0xff841426, 0xff530C17, "deprecated"), PATCHWORK_FORGE(
			"modscreen.badge.forge",
			0xff1f2d42,
			0xff101721,
			null
		), MODPACK("modscreen.badge.modpack", 0xff7a2b7c, 0xff510d54, null), MINECRAFT("modscreen.badge.minecraft",
			0xff6f6c6a,
			0xff31302f,
			null
		);

		private final Component text;
		private final int outlineColor, fillColor;
		private final String key;
		private static final Map<String, Badge> KEY_MAP = new HashMap<>();

		Badge(String translationKey, int outlineColor, int fillColor, String key) {
			this.text = Component.translatable(translationKey);
			this.outlineColor = outlineColor;
			this.fillColor = fillColor;
			this.key = key;
		}

		public Component getText() {
			return this.text;
		}

		public int getOutlineColor() {
			return this.outlineColor;
		}

		public int getFillColor() {
			return this.fillColor;
		}

		public static Set<Badge> convert(Set<String> badgeKeys, String modId) {
			return badgeKeys.stream().map(key -> {
				if (!KEY_MAP.containsKey(key)) {
					NotebookModScreen.LOGGER.warn("Skipping unknown badge key '{}' specified by mod '{}'", key, modId);
				}

				return KEY_MAP.get(key);
			}).filter(Objects::nonNull).collect(Collectors.toSet());
		}

		static {
			Arrays.stream(values()).forEach(badge -> KEY_MAP.put(badge.key, badge));
		}
	}
}

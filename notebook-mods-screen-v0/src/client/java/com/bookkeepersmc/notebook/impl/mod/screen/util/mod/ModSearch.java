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

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Pair;

import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;

public class ModSearch {

	public static boolean validSearchQuery(String query) {
		return query != null && !query.isEmpty();
	}

	public static List<Mod> search(ModsScreen screen, String query, List<Mod> candidates) {
		if (!validSearchQuery(query)) {
			return candidates;
		}
		return candidates.stream()
			.map(modContainer -> new Pair<>(modContainer,
				passesFilters(screen, modContainer, query.toLowerCase(Locale.ROOT))
			))
			.filter(pair -> pair.getRight() > 0)
			.sorted((a, b) -> b.getRight() - a.getRight())
			.map(Pair::getLeft)
			.collect(Collectors.toList());
	}

	private static int passesFilters(ModsScreen screen, Mod mod, String query) {
		String modId = mod.getId();
		String modName = mod.getName();
		String modTranslatedName = mod.getTranslatedName();
		String modDescription = mod.getDescription();
		String modSummary = mod.getSummary();

		String library = I18n.translate("modscreen.searchTerms.library");
		String patchwork = I18n.translate("modscreen.searchTerms.patchwork");
		String modpack = I18n.translate("modscreen.searchTerms.modpack");
		String deprecated = I18n.translate("modscreen.searchTerms.deprecated");
		String clientside = I18n.translate("modscreen.searchTerms.clientside");
		String configurable = I18n.translate("modscreen.searchTerms.configurable");
		String hasUpdate = I18n.translate("modscreen.searchTerms.hasUpdate");

		// Libraries are currently hidden, ignore them entirely
		if (mod.isHidden() || !ModScreenConfig.SHOW_LIBRARIES.getValue() && mod.getBadges().contains(Mod.Badge.LIBRARY)) {
			return 0;
		}

		// Some basic search, could do with something more advanced but this will do for now
		if (modName.toLowerCase(Locale.ROOT).contains(query) // Search default mod name
			|| modTranslatedName.toLowerCase(Locale.ROOT).contains(query) // Search localized mod name
			|| modId.toLowerCase(Locale.ROOT).contains(query) // Search mod ID
		) {
			return query.length() >= 3 ? 2 : 1;
		}

		if (modDescription.toLowerCase(Locale.ROOT).contains(query) // Search default mod description
			|| modSummary.toLowerCase(Locale.ROOT).contains(query) // Search mod summary
			|| authorMatches(mod, query) // Search via author
			|| library.contains(query) && mod.getBadges().contains(Mod.Badge.LIBRARY) // Search for lib mods
			|| patchwork.contains(query) && mod.getBadges()
			.contains(Mod.Badge.PATCHWORK_FORGE) // Search for patchwork mods
			|| modpack.contains(query) && mod.getBadges().contains(Mod.Badge.MODPACK) // Search for modpack mods
			|| deprecated.contains(query) && mod.getBadges()
			.contains(Mod.Badge.DEPRECATED) // Search for deprecated mods
			|| clientside.contains(query) && mod.getBadges().contains(Mod.Badge.CLIENT) // Search for clientside mods
			|| configurable.contains(query) && screen.getModHasConfigScreen()
			.get(modId) // Search for mods that can be configured
			|| hasUpdate.contains(query) && mod.hasUpdate() // Search for mods that have updates
		) {
			return 1;
		}

		// Allow parent to pass filter if a child passes
		if (NotebookModScreen.PARENT_MAP.keySet().contains(mod)) {
			for (Mod child : NotebookModScreen.PARENT_MAP.get(mod)) {
				int result = passesFilters(screen, child, query);

				if (result > 0) {
					return result;
				}
			}
		}
		return 0;
	}

	private static boolean authorMatches(Mod mod, String query) {
		return mod.getAuthors()
			.stream()
			.map(s -> s.toLowerCase(Locale.ROOT))
			.anyMatch(s -> s.contains(query.toLowerCase(Locale.ROOT)));
	}

}

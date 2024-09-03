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
package com.bookkeepersmc.notebook.impl.tag.common.v1;

import java.util.List;
import java.util.Locale;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import com.bookkeepersmc.api.ModInitializer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.notebook.api.event.lifecycle.v1.ServerLifecycleEvents;

public class TranslationConventionLogWarnings implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranslationConventionLogWarnings.class);

	private static final LogWarningMode LOG_UNTRANSLATED_WARNING_MODE = setupLogWarningModeProperty();

	private static LogWarningMode setupLogWarningModeProperty() {
		final LogWarningMode defaultMode = NotebookLoader.getInstance().isDevelopmentEnvironment() ? LogWarningMode.SHORT : LogWarningMode.SILENCED;
		String property = System.getProperty("notebook-tag-conventions-v2.missingTagTranslationWarning", defaultMode.name()).toUpperCase(Locale.ROOT);

		try {
			return LogWarningMode.valueOf(property);
		} catch (Exception e) {
			LOGGER.error("Unknown entry `{}` for property `notebook-tag-conventions-v2.missingTagTranslationWarning`.", property);
			return LogWarningMode.SILENCED;
		}
	}

	private enum LogWarningMode {
		SILENCED,
		SHORT,
		VERBOSE,
		FAIL;

		boolean verbose() {
			return this == VERBOSE || this == FAIL;
		}
	}

	public void onInitialize() {
		if (LOG_UNTRANSLATED_WARNING_MODE != LogWarningMode.SILENCED) {
			setupUntranslatedItemTagWarning();
		}
	}

	private static void setupUntranslatedItemTagWarning() {
		// Log missing item tag translations only when world is started.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Language language = Language.getInstance();
			Registry<Item> itemRegistry = server.getRegistryManager().getLookupOrThrow(Registries.ITEM);
			List<TagKey<Item>> untranslatedItemTags = new ObjectArrayList<>();
			itemRegistry.getTags().forEach(itemTagKey -> {
				// We do not translate vanilla's tags at this moment.
				if (itemTagKey.getKey().id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
					return;
				}

				if (!language.hasTranslation(itemTagKey.getKey().getTranslationKey())) {
					untranslatedItemTags.add(itemTagKey.getKey());
				}
			});

			if (untranslatedItemTags.isEmpty()) {
				return;
			}

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("""
					\n	Dev warning - Untranslated Item Tags detected. Please translate your item tags so other mods such as recipe viewers can properly display your tag's name.
						The format desired is tag.item.<namespace>.<path> for the translation key with slashes in path turned into periods.
						To disable this message, set this system property in your runs: `-Dnotebook-tag-conventions-v2.missingTagTranslationWarning=SILENCED`.
						To see individual untranslated item tags found, set the system property to `-Dnotebook-tag-conventions-v2.missingTagTranslationWarning=VERBOSE`.
						Default is `SHORT`.
					""");

			// Print out all untranslated tags when desired.
			if (LOG_UNTRANSLATED_WARNING_MODE.verbose()) {
				stringBuilder.append("\nUntranslated item tags:");

				for (TagKey<Item> tagKey : untranslatedItemTags) {
					stringBuilder.append("\n     ").append(tagKey.id());
				}
			}

			LOGGER.warn(stringBuilder.toString());

			if (LOG_UNTRANSLATED_WARNING_MODE == LogWarningMode.FAIL) {
				throw new RuntimeException("Tag translation validation failed");
			}
		});
	}
}

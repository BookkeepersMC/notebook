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
package com.bookkeepersmc.notebook.impl.mod.screen;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import com.bookkeepersmc.api.ClientModInitializer;
import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.loader.api.metadata.ModMetadata;
import com.bookkeepersmc.notebook.api.mod.screen.v0.ConfigScreenFactory;
import com.bookkeepersmc.notebook.api.mod.screen.v0.ModScreenCreator;
import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateChecker;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfigManager;
import com.bookkeepersmc.notebook.impl.mod.screen.event.ModScreenEventHandler;
import com.bookkeepersmc.notebook.impl.mod.screen.util.EnumToLowerCaseJsonConverter;
import com.bookkeepersmc.notebook.impl.mod.screen.util.ModScreenTexts;
import com.bookkeepersmc.notebook.impl.mod.screen.util.UpdateCheckerUtil;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.notebook.DummyParentMod;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.notebook.NotebookMod;

public class NotebookModScreen implements ClientModInitializer {
	public static final String MOD_ID = "notebook-mods-screen-v0";
	public static final Logger LOGGER = LoggerFactory.getLogger("Notebook Mod Screen");
	public static final Gson GSON;
	public static final Gson GSON_MINIFIED;

	static {
		GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Enum.class,
				new EnumToLowerCaseJsonConverter()
			)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		GSON = builder.setPrettyPrinting().create();
		GSON_MINIFIED = builder.create();
	}

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static final Map<String, ConfigScreenFactory<?>> configScreenFactories = new HashMap<>();
	private static final List<ModScreenCreator> apiImplementations = new ArrayList<>();

	private static int cachedDisplayedModCount = -1;
	public static final boolean DEV_ENVIRONMENT = NotebookLoader.getInstance().isDevelopmentEnvironment();

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		for (ModScreenCreator api : apiImplementations) {
			var factoryProviders = api.getProvidedConfigScreenFactories();
			if (!factoryProviders.isEmpty()) {
				factoryProviders.forEach(configScreenFactories::putIfAbsent);
			}
		}
		if (ModScreenConfig.HIDDEN_CONFIGS.getValue().contains(modid)) {
			return null;
		}
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		if (factory != null) {
			return factory.create(menuScreen);
		}
		return null;
	}

	@Override
	public void onInitializeClient() {
		ModScreenConfigManager.initializeConfig();
		Set<String> modpackMods = new HashSet<>();
		Map<String, UpdateChecker> updateCheckers = new HashMap<>();
		Map<String, UpdateChecker> providedUpdateCheckers = new HashMap<>();

		// Ignore deprecations, they're from Quilt Loader being in the dev env
		//noinspection deprecation
		NotebookLoader.getInstance().getEntrypointContainers("modscreen", ModScreenCreator.class).forEach(entrypoint -> {
			//noinspection deprecation
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ModScreenCreator api = entrypoint.getEntrypoint();
				configScreenFactories.put(modId, api.getModConfigScreenFactory());
				apiImplementations.add(api);
				updateCheckers.put(modId, api.getUpdateChecker());
				providedUpdateCheckers.putAll(api.getProvidedUpdateCheckers());
				api.attachModpackBadges(modpackMods::add);
			} catch (Throwable e) {
				LOGGER.error("Mod {} provides a broken implementation of ModScreenCreator", modId, e);
			}
		});

		// Fill mods map
		//noinspection deprecation
		for (ModContainer modContainer : NotebookLoader.getInstance().getAllMods()) {
			Mod mod = new NotebookMod(modContainer, modpackMods);


			var updateChecker = updateCheckers.get(mod.getId());

			if (updateChecker == null) {
				updateChecker = providedUpdateCheckers.get(mod.getId());
			}

			MODS.put(mod.getId(), mod);
			mod.setUpdateChecker(updateChecker);
		}

		checkForUpdates();

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId != null) {
				Mod parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof NotebookMod) {
						parent = new DummyParentMod((NotebookMod) mod, parentId);
						dummyParents.put(parentId, parent);
					}
				}
				PARENT_MAP.put(parent, mod);
			} else {
				ROOT_MODS.put(mod.getId(), mod);
			}
		}
		MODS.putAll(dummyParents);
		ModScreenEventHandler.register();
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public static void checkForUpdates() {
		UpdateCheckerUtil.checkForUpdates();
	}

	public static boolean areModUpdatesAvailable() {
		if (!ModScreenConfig.UPDATE_CHECKER.getValue()) {
			return false;
		}

		for (Mod mod : MODS.values()) {
			if (mod.isHidden()) {
				continue;
			}

			if (!ModScreenConfig.SHOW_LIBRARIES.getValue() && mod.getBadges().contains(Mod.Badge.LIBRARY)) {
				continue;
			}

			if (mod.hasUpdate() || mod.getChildHasUpdate()) {
				return true; // At least one currently visible mod has an update
			}
		}

		return false;
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			boolean includeChildren = ModScreenConfig.COUNT_CHILDREN.getValue();
			boolean includeLibraries = ModScreenConfig.COUNT_LIBRARIES.getValue();
			boolean includeHidden = ModScreenConfig.COUNT_HIDDEN_MODS.getValue();

			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod -> {
				boolean isChild = mod.getParent() != null;
				if (!includeChildren && isChild) {
					return false;
				}
				boolean isLibrary = mod.getBadges().contains(Mod.Badge.LIBRARY);
				if (!includeLibraries && isLibrary) {
					return false;
				}
				return includeHidden || !mod.isHidden();
			}).count());
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}

	public static Component createModsButtonText(boolean title) {
		String count = NotebookModScreen.getDisplayedModCount();
		String specificKey = "modscreen.loaded." + count;
		String key = I18n.exists(specificKey) ? specificKey : "modscreen.loaded";

		Component numberOnText = Component.literal(count);

		return numberOnText.copy().append(" ").append(ModScreenTexts.TITLE);
	}
}

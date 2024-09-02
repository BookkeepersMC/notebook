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
package com.bookkeepersmc.notebook.api.mod.screen.v0;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;

public interface ModScreenCreator {

	/**
	 * Used for creating a {@link Screen} instance of the Mod Menu
	 * "Mods" screen
	 *
	 * @param previous The screen before opening
	 * @return A "Mods" Screen
	 */
	static Screen createModsScreen(Screen previous) {
		return new ModsScreen(previous);
	}

	/**
	 * Used for creating a {@link Text} just like what would appear
	 * on a Mod Menu Mods button
	 *
	 * @return The text that would be displayed on a Mods button
	 */
	static Text createModsButtonText() {
		return NotebookModScreen.createModsButtonText(true);
	}

	/**
	 * Used to construct a new config screen instance when your mod's
	 * configuration button is selected on the mod menu screen. The
	 * screen instance parameter is the active mod menu screen.
	 *
	 * @return A factory for constructing config screen instances.
	 */
	default ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> null;
	}

	/**
	 * Used for mods that have their own update checking logic.
	 * By returning your own {@link UpdateChecker} instance, you can override ModMenus built-in update checking logic.
	 *
	 * @return An {@link UpdateChecker} or <code>null</code> if ModMenu should handle update checking.
	 */
	default UpdateChecker getUpdateChecker() {
		return null;
	}

	/**
	 * Used to provide config screen factories for other mods. This takes second
	 * priority to a mod's own config screen factory provider. For example, if
	 * mod `xyz` supplies a config screen factory, mod `abc` providing a config
	 * screen to `xyz` will be pointless, as the one provided by `xyz` will be
	 * used.
	 * <p>
	 * This method is NOT meant to be used to add a config screen factory to
	 * your own mod.
	 *
	 * @return a map of mod ids to screen factories.
	 */
	default Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return Map.of();
	}

	/**
	 * Used to provide update checkers for other mods. A mod registering its own
	 * update checker will take priority over any provided ones should both exist.
	 *
	 * @return a map of mod ids to update checkers.
	 */
	default Map<String, UpdateChecker> getProvidedUpdateCheckers() {
		return Map.of();
	}

	/**
	 * Used to mark mods with a badge indicating that they are
	 * provided by a modpack.
	 * <p>
	 * Builtin mods such as `minecraft` cannot be marked as
	 * provided by a modpack.
	 */
	default void attachModpackBadges(Consumer<String> consumer) {
	}
}

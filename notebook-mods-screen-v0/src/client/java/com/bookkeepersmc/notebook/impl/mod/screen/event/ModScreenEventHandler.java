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
package com.bookkeepersmc.notebook.impl.mod.screen.event;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.client.event.lifecycle.v1.ClientTickEvents;
import com.bookkeepersmc.notebook.api.client.keybind.v1.KeybindHelper;
import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenEvents;
import com.bookkeepersmc.notebook.api.client.screen.v1.Screens;
import com.bookkeepersmc.notebook.api.mod.screen.v0.ModScreenCreator;
import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.ModsScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.ModScreenButtonWidget;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.UpdateCheckerTexturedButtonWidget;
import com.bookkeepersmc.notebook.impl.mod.screen.util.UpdateCheckerUtil;

public class ModScreenEventHandler {
	public static final ResourceLocation MODS_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(NotebookModScreen.MOD_ID, "textures/gui/mods_button.png");
	private static KeyMapping MENU_KEY_BIND;

	public static void register() {
		MENU_KEY_BIND = KeybindHelper.registerKeybind(new KeyMapping("key.modmenu.open_menu",
			InputConstants.Type.KEYSYM,
			InputConstants.UNKNOWN.getValue(),
			"key.categories.misc"
		));
		ClientTickEvents.END_CLIENT_TICK.register(ModScreenEventHandler::onClientEndTick);

		ScreenEvents.AFTER_INIT.register(ModScreenEventHandler::afterScreenInit);
	}

	public static void afterScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (screen instanceof TitleScreen) {
			afterTitleScreenInit(screen);
		}
	}

	private static void afterTitleScreenInit(Screen screen) {
		final List<AbstractWidget> buttons = Screens.getWidgets(screen);
		if (ModScreenConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				AbstractWidget widget = buttons.get(i);
				if (widget instanceof Button button) {
					if (ModScreenConfig.MODS_BUTTON_STYLE.getValue() == ModScreenConfig.TitleMenuButtonStyle.CLASSIC) {
						if (button.visible) {
							shiftButtons(button, modsButtonIndex == -1, spacing);
							if (modsButtonIndex == -1) {
								buttonsY = button.getY();
							}
						}
					}
					if (buttonHasText(button, "menu.online")) {
						if (ModScreenConfig.MODS_BUTTON_STYLE.getValue() ==
							ModScreenConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
							buttons.set(i, new ModScreenButtonWidget(button.getX(),
								button.getY(),
								button.getWidth(),
								button.getHeight(),
								ModScreenCreator.createModsButtonText(),
								screen
							));
						} else {
							if (ModScreenConfig.MODS_BUTTON_STYLE.getValue() ==
								ModScreenConfig.TitleMenuButtonStyle.SHRINK) {
								button.setWidth(98);
							}
							modsButtonIndex = i + 1;
							if (button.visible) {
								buttonsY = button.getY();
							}
						}
					}
				}

			}
			if (modsButtonIndex != -1) {
				if (ModScreenConfig.MODS_BUTTON_STYLE.getValue() == ModScreenConfig.TitleMenuButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModScreenButtonWidget(screen.width / 2 - 100,
						buttonsY + spacing,
						200,
						20,
						ModScreenCreator.createModsButtonText(),
						screen
					));
				} else if (ModScreenConfig.MODS_BUTTON_STYLE.getValue() == ModScreenConfig.TitleMenuButtonStyle.SHRINK) {
					buttons.add(modsButtonIndex,
						new ModScreenButtonWidget(screen.width / 2 + 2,
							buttonsY,
							98,
							20,
							ModScreenCreator.createModsButtonText(),
							screen
						)
					);
				} else if (ModScreenConfig.MODS_BUTTON_STYLE.getValue() == ModScreenConfig.TitleMenuButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(screen.width / 2 + 104,
						buttonsY,
						20,
						20,
						0,
						0,
						20,
						MODS_BUTTON_TEXTURE,
						32,
						64,
						button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)),
						ModScreenCreator.createModsButtonText()
					));
				}
			}
		}
		UpdateCheckerUtil.triggerV2DeprecatedToast();
	}

	private static void onClientEndTick(Minecraft client) {
		while (MENU_KEY_BIND.consumeClick()) {
			client.setScreen(new ModsScreen(client.screen));
		}
	}

	public static boolean buttonHasText(LayoutElement widget, String... translationKeys) {
		if (widget instanceof Button button) {
			Component text = button.getMessage();
			ComponentContents textContent = text.getContents();

			return textContent instanceof TranslatableContents && Arrays.stream(translationKeys)
				.anyMatch(s -> ((TranslatableContents) textContent).getKey().equals(s));
		}
		return false;
	}

	public static void shiftButtons(LayoutElement widget, boolean shiftUp, int spacing) {
		if (shiftUp) {
			widget.setY(widget.getY() - spacing / 2);
		} else if (!(widget instanceof AbstractWidget button &&
			button.getMessage().equals(Component.translatable("title.credits"))
		)) {
			widget.setY(widget.getY() + spacing / 2);
		}
	}
}

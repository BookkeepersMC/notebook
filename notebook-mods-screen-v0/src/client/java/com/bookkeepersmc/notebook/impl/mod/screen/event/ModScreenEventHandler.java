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

import com.mojang.blaze3d.platform.InputUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.option.KeyBind;
import net.minecraft.text.Text;
import net.minecraft.text.component.TextComponent;
import net.minecraft.text.component.TranslatableComponent;
import net.minecraft.util.Identifier;

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
	public static final Identifier MODS_BUTTON_TEXTURE = Identifier.of(NotebookModScreen.MOD_ID, "textures/gui/mods_button.png");
	private static KeyBind MENU_KEY_BIND;

	public static void register() {
		MENU_KEY_BIND = KeybindHelper.registerKeybind(new KeyBind("key.modscreen.open_menu",
				InputUtil.Type.KEYSYM,
				InputUtil.UNKNOWN_KEY.getKeyCode(),
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
		final List<ClickableWidget> buttons = Screens.getWidgets(screen);
		if (ModScreenConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				ClickableWidget widget = buttons.get(i);
				if (widget instanceof ButtonWidget button) {
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
		while (MENU_KEY_BIND.wasPressed()) {
			client.setScreen(new ModsScreen(client.currentScreen));
		}
	}

	public static boolean buttonHasText(Widget widget, String... translationKeys) {
		if (widget instanceof ButtonWidget button) {
			Text text = button.getMessage();
			TextComponent textContent = text.asComponent();

			return textContent instanceof TranslatableComponent && Arrays.stream(translationKeys)
					.anyMatch(s -> ((TranslatableComponent) textContent).getKey().equals(s));
		}
		return false;
	}

	public static void shiftButtons(Widget widget, boolean shiftUp, int spacing) {
		if (shiftUp) {
			widget.setY(widget.getY() - spacing / 2);
		} else if (!(widget instanceof ClickableWidget button &&
				button.getMessage().equals(Text.translatable("title.credits"))
		)) {
			widget.setY(widget.getY() + spacing / 2);
		}
	}
}

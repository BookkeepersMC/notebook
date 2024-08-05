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
package com.bookkeepersmc.notebook.mixin.mod.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;

import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		if (ModScreenConfig.MODIFY_TITLE_SCREEN.getValue() && ModScreenConfig.MODS_BUTTON_STYLE.getValue() == ModScreenConfig.TitleMenuButtonStyle.CLASSIC) {
			return height - 51;
		} else if (ModScreenConfig.MODS_BUTTON_STYLE.getValue() == ModScreenConfig.TitleMenuButtonStyle.REPLACE_REALMS || ModScreenConfig.MODS_BUTTON_STYLE.getValue() == ModScreenConfig.TitleMenuButtonStyle.SHRINK) {
			return -99999;
		}
		return height;
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I", ordinal = 0))
	private String onRender(String string) {
		if (ModScreenConfig.MODIFY_TITLE_SCREEN.getValue() && ModScreenConfig.MOD_COUNT_LOCATION.getValue()
			.isOnTitleScreen()) {
			String count = NotebookModScreen.getDisplayedModCount();
			String specificKey = "modscreen.mods." + count;
			String replacementKey = I18n.exists(specificKey) ? specificKey : "modscreen.mods.n";
			if (ModScreenConfig.EASTER_EGGS.getValue() && I18n.exists(specificKey + ".secret")) {
				replacementKey = specificKey + ".secret";
			}
			return string.replace(I18n.get(I18n.get("menu.modded")), I18n.get(replacementKey, count));
		}
		return string;
	}
}

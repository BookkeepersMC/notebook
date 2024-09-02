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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.layout.GridWidget;
import net.minecraft.text.Text;

import com.bookkeepersmc.notebook.api.mod.screen.v0.ModScreenCreator;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.event.ModScreenEventHandler;
import com.bookkeepersmc.notebook.impl.mod.screen.gui.widget.ModScreenButtonWidget;

@Mixin(GameMenuScreen.class)
public abstract class MixinGameMenu extends Screen {
	protected MixinGameMenu(Text title) {
		super(title);
	}

	@Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/layout/GridWidget;visitWidgets(Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onInitWidgets(CallbackInfo ci, GridWidget gridWidget) {
		if (gridWidget != null) {
			final List<Widget> buttons = ((AccessorGridWidget) gridWidget).getChildren();
			if (ModScreenConfig.MODIFY_GAME_MENU.getValue()) {
				int modsButtonIndex = -1;
				final int spacing = 24;
				int buttonsY = this.height - 50;
				int vanillaButtonsY = this.height / 4 + 72 - 16 + 1;
				final int fullWidthButton = 204;

				for (int i = 0; i < buttons.size(); i++) {
					Widget widget = buttons.get(i);
						if (!(widget instanceof ClickableWidget button) || button.visible) {
							ModScreenEventHandler.shiftButtons(widget, modsButtonIndex == -1 || ModScreenEventHandler.buttonHasText(widget, "menu.reportBugs", "menu.server_links"), spacing);
							ModScreenEventHandler.shiftButtons(widget, modsButtonIndex == 1 || ModScreenEventHandler.buttonHasText(widget, "menu.options", "menu.shareToLan"), spacing);
							if (modsButtonIndex == -1) {
								buttonsY = widget.getY();
							}
						}

					boolean isShortFeedback = ModScreenEventHandler.buttonHasText(widget, "menu.feedback");
					boolean isLongFeedback = ModScreenEventHandler.buttonHasText(widget, "menu.sendFeedback");

					if (isShortFeedback || isLongFeedback) {
						modsButtonIndex = i + 1;
						vanillaButtonsY = widget.getY();
							modsButtonIndex = i + 1;
							if (!(widget instanceof ClickableWidget button) || button.visible) {
								buttonsY = widget.getY();
							}

					}
				}
				if (modsButtonIndex != -1) {
						buttons.add(modsButtonIndex, new ModScreenButtonWidget(
							this.width / 2 - 102,
							buttonsY + 48,
							fullWidthButton,
							20,
							ModScreenCreator.createModsButtonText(),
							this
						));
				}
			}
		}
	}
}

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
package com.bookkeepersmc.notebook.mixin.screen;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenMouseEvents;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Unique
	private Screen currentScreen;

	// private synthetic method_1611([ZDDI)V
	@Inject(method = "method_1611([ZLnet/minecraft/client/gui/screens/Screen;DDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"), cancellable = true)
	private static void beforeMouseClickedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		@SuppressWarnings("resource")
		MouseHandlerMixin thisRef = (MouseHandlerMixin) (Object) Minecraft.getInstance().mouseHandler;
		// Store the screen in a variable in case someone tries to change the screen during this before event.
		// If someone changes the screen, the after event will likely have class cast exceptions or throw a NPE.
		thisRef.currentScreen = thisRef.minecraft.screen;

		if (thisRef.currentScreen == null) {
			return;
		}

		if (!ScreenMouseEvents.allowMouseClick(thisRef.currentScreen).invoker().allowMouseClick(thisRef.currentScreen, mouseX, mouseY, button)) {
			resultHack[0] = true; // Set this press action as handled.
			thisRef.currentScreen = null;
			ci.cancel(); // Exit the lambda
			return;
		}

		ScreenMouseEvents.beforeMouseClick(thisRef.currentScreen).invoker().beforeMouseClick(thisRef.currentScreen, mouseX, mouseY, button);
	}

	// private synthetic method_1611([ZDDI)V
	@Inject(method = "method_1611([ZLnet/minecraft/client/gui/screens/Screen;DDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z", shift = At.Shift.AFTER))
	private static void afterMouseClickedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		@SuppressWarnings("resource")
		MouseHandlerMixin thisRef = (MouseHandlerMixin) (Object) Minecraft.getInstance().mouseHandler;

		if (thisRef.currentScreen == null) {
			return;
		}

		ScreenMouseEvents.afterMouseClick(thisRef.currentScreen).invoker().afterMouseClick(thisRef.currentScreen, mouseX, mouseY, button);
		thisRef.currentScreen = null;
	}

	// private synthetic method_1605([ZDDI)V
	@Inject(method = "method_1605([ZLnet/minecraft/client/gui/screens/Screen;DDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(DDI)Z"), cancellable = true)
	private static void beforeMouseReleasedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		@SuppressWarnings("resource")
		MouseHandlerMixin thisRef = (MouseHandlerMixin) (Object) Minecraft.getInstance().mouseHandler;

		// Store the screen in a variable in case someone tries to change the screen during this before event.
		// If someone changes the screen, the after event will likely have class cast exceptions or throw a NPE.
		thisRef.currentScreen = thisRef.minecraft.screen;

		if (thisRef.currentScreen == null) {
			return;
		}

		if (!ScreenMouseEvents.allowMouseRelease(thisRef.currentScreen).invoker().allowMouseRelease(thisRef.currentScreen, mouseX, mouseY, button)) {
			resultHack[0] = true; // Set this press action as handled.
			thisRef.currentScreen = null;
			ci.cancel(); // Exit the lambda
			return;
		}

		ScreenMouseEvents.beforeMouseRelease(thisRef.currentScreen).invoker().beforeMouseRelease(thisRef.currentScreen, mouseX, mouseY, button);
	}

	// private synthetic method_1605([ZDDI)V
	@Inject(method = "method_1605([ZLnet/minecraft/client/gui/screens/Screen;DDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(DDI)Z", shift = At.Shift.AFTER))
	private static void afterMouseReleasedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		@SuppressWarnings("resource")
		MouseHandlerMixin thisRef = (MouseHandlerMixin) (Object) Minecraft.getInstance().mouseHandler;

		if (thisRef.currentScreen == null) {
			return;
		}

		ScreenMouseEvents.afterMouseRelease(thisRef.currentScreen).invoker().afterMouseRelease(thisRef.currentScreen, mouseX, mouseY, button);
		thisRef.currentScreen = null;
	}

	@Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void beforeMouseScrollEvent(long window, double horizontal, double vertical, CallbackInfo ci, @Local(ordinal = 1) double horizontalAmount, @Local(ordinal = 2) double verticalAmount, @Local(ordinal = 3) double mouseX, @Local(ordinal = 4) double mouseY) {
		// Store the screen in a variable in case someone tries to change the screen during this before event.
		// If someone changes the screen, the after event will likely have class cast exceptions or throw a NPE.
		this.currentScreen = this.minecraft.screen;

		if (this.currentScreen == null) {
			return;
		}

		if (!ScreenMouseEvents.allowMouseScroll(this.currentScreen).invoker().allowMouseScroll(this.currentScreen, mouseX, mouseY, horizontalAmount, verticalAmount)) {
			this.currentScreen = null;
			ci.cancel();
			return;
		}

		ScreenMouseEvents.beforeMouseScroll(this.currentScreen).invoker().beforeMouseScroll(this.currentScreen, mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void afterMouseScrollEvent(long l, double d, double e, CallbackInfo ci, @Local(ordinal = 1) double horizontalAmount, @Local(ordinal = 2) double verticalAmount, @Local(ordinal = 3) double mouseX, @Local(ordinal = 4) double mouseY) {
		if (this.currentScreen == null) {
			return;
		}

		ScreenMouseEvents.afterMouseScroll(this.currentScreen).invoker().afterMouseScroll(this.currentScreen, mouseX, mouseY, horizontalAmount, verticalAmount);
		this.currentScreen = null;
	}
}

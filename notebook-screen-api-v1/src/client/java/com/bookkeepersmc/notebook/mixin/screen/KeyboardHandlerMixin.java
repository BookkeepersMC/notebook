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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;

import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenKeyboardEvents;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
	@Inject(method = "method_1454(ILnet/minecraft/client/gui/screens/Screen;[ZIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(III)Z"), cancellable = true)
	private static void beforeKeyPressedEvent(int code, Screen screen, boolean[] resultHack, int key, int scancode, int modifiers, CallbackInfo ci) {
		if (!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, key, scancode, modifiers)) {
			resultHack[0] = true; // Set this press action as handled.
			ci.cancel(); // Exit the lambda
			return;
		}

		ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, key, scancode, modifiers);
	}

	@Inject(method = "method_1454(ILnet/minecraft/client/gui/screens/Screen;[ZIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(III)Z", shift = At.Shift.AFTER))
	private static void afterKeyPressedEvent(int code, Screen screen, boolean[] resultHack, int key, int scancode, int modifiers, CallbackInfo ci) {
		ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, key, scancode, modifiers);
	}

	@Inject(method = "method_1454(ILnet/minecraft/client/gui/screens/Screen;[ZIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyReleased(III)Z"), cancellable = true)
	private static void beforeKeyReleasedEvent(int code, Screen screen, boolean[] resultHack, int key, int scancode, int modifiers, CallbackInfo ci) {
		if (!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, key, scancode, modifiers)) {
			resultHack[0] = true; // Set this press action as handled.
			ci.cancel(); // Exit the lambda
			return;
		}

		ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, key, scancode, modifiers);
	}

	@Inject(method = "method_1454(ILnet/minecraft/client/gui/screens/Screen;[ZIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyReleased(III)Z", shift = At.Shift.AFTER))
	private static void afterKeyReleasedEvent(int code, Screen screen, boolean[] resultHack, int key, int scancode, int modifiers, CallbackInfo ci) {
		ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, key, scancode, modifiers);
	}
}

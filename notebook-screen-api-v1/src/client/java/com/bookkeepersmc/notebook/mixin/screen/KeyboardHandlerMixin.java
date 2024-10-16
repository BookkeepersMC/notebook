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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.Screen;

import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenKeyboardEvents;

@Mixin(Keyboard.class)
abstract class KeyboardHandlerMixin {
	@WrapOperation(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(III)Z"))
	private boolean invokeKeyPressedEvents(Screen screen, int key, int scancode, int modifiers, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, key, scancode, modifiers)) {
				// Set this press action as handled
				return true;
			}

			ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, key, scancode, modifiers);
		}

		boolean result = operation.call(screen, key, scancode, modifiers);

		if (screen != null) {
			ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, key, scancode, modifiers);
		}

		return result;
	}

	@WrapOperation(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyReleased(III)Z"))
	private boolean invokeKeyReleasedEvents(Screen screen, int key, int scancode, int modifiers, Operation<Boolean> operation) {
		// The screen passed to events is the same as the screen the handler method is called on,
		// regardless of whether the screen changes within the handler or event invocations.

		if (screen != null) {
			if (!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, key, scancode, modifiers)) {
				// Set this release action as handled
				return true;
			}

			ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, key, scancode, modifiers);
		}

		boolean result = operation.call(screen, key, scancode, modifiers);

		if (screen != null) {
			ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, key, scancode, modifiers);
		}

		return result;
	}
}

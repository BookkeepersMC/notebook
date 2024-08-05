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
package com.bookkeepersmc.notebook.api.client.screen.v1;

import java.util.Objects;

import net.minecraft.client.gui.screens.Screen;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.impl.client.screen.ScreenExtensions;

public final class ScreenKeyboardEvents {
	public static Event<AllowKeyPress> allowKeyPress(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAllowKeyPressEvent();
	}

	public static Event<BeforeKeyPress> beforeKeyPress(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getBeforeKeyPressEvent();
	}

	public static Event<AfterKeyPress> afterKeyPress(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAfterKeyPressEvent();
	}

	public static Event<AllowKeyRelease> allowKeyRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAllowKeyReleaseEvent();
	}

	public static Event<BeforeKeyRelease> beforeKeyRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getBeforeKeyReleaseEvent();
	}

	public static Event<AfterKeyRelease> afterKeyRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAfterKeyReleaseEvent();
	}

	private ScreenKeyboardEvents() {
	}

	@FunctionalInterface
	public interface AllowKeyPress {
		boolean allowKeyPress(Screen screen, int key, int scancode, int modifiers);
	}

	@FunctionalInterface
	public interface BeforeKeyPress {
		void beforeKeyPress(Screen screen, int key, int scancode, int modifiers);
	}

	@FunctionalInterface
	public interface AfterKeyPress {
		void afterKeyPress(Screen screen, int key, int scancode, int modifiers);
	}

	@FunctionalInterface
	public interface AllowKeyRelease {
		boolean allowKeyRelease(Screen screen, int key, int scancode, int modifiers);
	}

	@FunctionalInterface
	public interface BeforeKeyRelease {
		void beforeKeyRelease(Screen screen, int key, int scancode, int modifiers);
	}

	@FunctionalInterface
	public interface AfterKeyRelease {
		void afterKeyRelease(Screen screen, int key, int scancode, int modifiers);
	}
}

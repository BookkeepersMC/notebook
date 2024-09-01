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

import net.minecraft.client.gui.screen.Screen;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.impl.client.screen.ScreenExtensions;

public final class ScreenMouseEvents {
	public static Event<AllowMouseClick> allowMouseClick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAllowMouseClickEvent();
	}

	public static Event<BeforeMouseClick> beforeMouseClick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getBeforeMouseClickEvent();
	}

	public static Event<AfterMouseClick> afterMouseClick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAfterMouseClickEvent();
	}

	public static Event<AllowMouseRelease> allowMouseRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAllowMouseReleaseEvent();
	}

	public static Event<BeforeMouseRelease> beforeMouseRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getBeforeMouseReleaseEvent();
	}

	public static Event<AfterMouseRelease> afterMouseRelease(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAfterMouseReleaseEvent();
	}

	public static Event<AllowMouseScroll> allowMouseScroll(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAllowMouseScrollEvent();
	}

	public static Event<BeforeMouseScroll> beforeMouseScroll(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getBeforeMouseScrollEvent();
	}

	public static Event<AfterMouseScroll> afterMouseScroll(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAfterMouseScrollEvent();
	}

	private ScreenMouseEvents() {
	}

	@FunctionalInterface
	public interface AllowMouseClick {
		boolean allowMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface BeforeMouseClick {
		void beforeMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AfterMouseClick {
		void afterMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AllowMouseRelease {
		boolean allowMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface BeforeMouseRelease {
		void beforeMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AfterMouseRelease {
		void afterMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface AllowMouseScroll {
		boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
	}

	@FunctionalInterface
	public interface BeforeMouseScroll {
		void beforeMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
	}

	@FunctionalInterface
	public interface AfterMouseScroll {
		void afterMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
	}
}

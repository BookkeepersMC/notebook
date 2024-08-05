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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;
import com.bookkeepersmc.notebook.impl.client.screen.ScreenExtensions;

public final class ScreenEvents {

	public static final Event<BeforeInit> BEFORE_INIT = EventFactory.createArrayBacked(BeforeInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (BeforeInit callback : callbacks) {
			callback.beforeInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	public static final Event<AfterInit> AFTER_INIT = EventFactory.createArrayBacked(AfterInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (AfterInit callback : callbacks) {
			callback.afterInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	public static Event<Remove> remove(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getRemoveEvent();
	}

	public static Event<BeforeRender> beforeRender(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getBeforeRenderEvent();
	}

	public static Event<AfterRender> afterRender(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAfterRenderEvent();
	}

	public static Event<BeforeTick> beforeTick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getBeforeTickEvent();
	}

	public static Event<AfterTick> afterTick(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ScreenExtensions.getExtensions(screen).notebook_getAfterTickEvent();
	}

	@FunctionalInterface
	public interface BeforeInit {
		void beforeInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight);
	}

	@FunctionalInterface
	public interface AfterInit {
		void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight);
	}

	@FunctionalInterface
	public interface Remove {
		void onRemove(Screen screen);
	}

	@FunctionalInterface
	public interface BeforeRender {
		void beforeRender(Screen screen, GuiGraphics drawContext, int mouseX, int mouseY, float tickDelta);
	}

	@FunctionalInterface
	public interface AfterRender {
		void afterRender(Screen screen, GuiGraphics drawContext, int mouseX, int mouseY, float tickDelta);
	}

	@FunctionalInterface
	public interface BeforeTick {
		void beforeTick(Screen screen);
	}

	@FunctionalInterface
	public interface AfterTick {
		void afterTick(Screen screen);
	}

	private ScreenEvents() {
	}
}

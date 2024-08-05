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
package com.bookkeepersmc.notebook.impl.client.screen;

import java.util.List;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenEvents;
import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenKeyboardEvents;
import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenMouseEvents;
import com.bookkeepersmc.notebook.api.event.Event;

public interface ScreenExtensions {
	static ScreenExtensions getExtensions(Screen screen) {
		return (ScreenExtensions) screen;
	}

	List<AbstractWidget> notebook_getButtons();

	Event<ScreenEvents.Remove> notebook_getRemoveEvent();

	Event<ScreenEvents.BeforeTick> notebook_getBeforeTickEvent();

	Event<ScreenEvents.AfterTick> notebook_getAfterTickEvent();

	Event<ScreenEvents.BeforeRender> notebook_getBeforeRenderEvent();

	Event<ScreenEvents.AfterRender> notebook_getAfterRenderEvent();

	// Keyboard

	Event<ScreenKeyboardEvents.AllowKeyPress> notebook_getAllowKeyPressEvent();

	Event<ScreenKeyboardEvents.BeforeKeyPress> notebook_getBeforeKeyPressEvent();

	Event<ScreenKeyboardEvents.AfterKeyPress> notebook_getAfterKeyPressEvent();

	Event<ScreenKeyboardEvents.AllowKeyRelease> notebook_getAllowKeyReleaseEvent();

	Event<ScreenKeyboardEvents.BeforeKeyRelease> notebook_getBeforeKeyReleaseEvent();

	Event<ScreenKeyboardEvents.AfterKeyRelease> notebook_getAfterKeyReleaseEvent();

	// Mouse

	Event<ScreenMouseEvents.AllowMouseClick> notebook_getAllowMouseClickEvent();

	Event<ScreenMouseEvents.BeforeMouseClick> notebook_getBeforeMouseClickEvent();

	Event<ScreenMouseEvents.AfterMouseClick> notebook_getAfterMouseClickEvent();

	Event<ScreenMouseEvents.AllowMouseRelease> notebook_getAllowMouseReleaseEvent();

	Event<ScreenMouseEvents.BeforeMouseRelease> notebook_getBeforeMouseReleaseEvent();

	Event<ScreenMouseEvents.AfterMouseRelease> notebook_getAfterMouseReleaseEvent();

	Event<ScreenMouseEvents.AllowMouseScroll> notebook_getAllowMouseScrollEvent();

	Event<ScreenMouseEvents.BeforeMouseScroll> notebook_getBeforeMouseScrollEvent();

	Event<ScreenMouseEvents.AfterMouseScroll> notebook_getAfterMouseScrollEvent();
}

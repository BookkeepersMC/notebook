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
package com.bookkeepersmc.notebook.api.client.rendering.v1;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;

/**
 * Called when the world renderer reloads, usually as result of changing resource pack
 * or video configuration, or when the player types F3+A in the debug screen.
 * Afterwards all render chunks will be reset and reloaded.
 *
 * <p>Render chunks and other render-related object instances will be made null
 * or invalid after this event so do not use it to capture dependent state.
 * Instead, use it to invalidate state and reinitialize lazily.
 */
public interface InvalidateRenderStateCallback {
	Event<InvalidateRenderStateCallback> EVENT = EventFactory.createArrayBacked(InvalidateRenderStateCallback.class,
			(listeners) -> () -> {
				for (InvalidateRenderStateCallback event : listeners) {
					event.onInvalidate();
				}
			}
	);

	void onInvalidate();
}

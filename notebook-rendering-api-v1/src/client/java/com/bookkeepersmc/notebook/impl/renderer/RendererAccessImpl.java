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
package com.bookkeepersmc.notebook.impl.renderer;

import com.bookkeepersmc.notebook.api.renderer.v1.Renderer;
import com.bookkeepersmc.notebook.api.renderer.v1.RendererAccess;

public final class RendererAccessImpl implements RendererAccess {
	public static final RendererAccessImpl INSTANCE = new RendererAccessImpl();

	private RendererAccessImpl() { }

	@Override
	public void registerRenderer(Renderer renderer) {
		if (renderer == null) {
			throw new NullPointerException("Attempt to register a NULL rendering plug-in.");
		} else if (activeRenderer != null) {
			throw new UnsupportedOperationException("A second rendering plug-in attempted to register. Multiple rendering plug-ins are not supported.");
		} else {
			activeRenderer = renderer;
			hasActiveRenderer = true;
		}
	}

	private Renderer activeRenderer = null;

	private boolean hasActiveRenderer = false;

	@Override
	public Renderer getRenderer() {
		return activeRenderer;
	}

	@Override
	public boolean hasRenderer() {
		return hasActiveRenderer;
	}
}

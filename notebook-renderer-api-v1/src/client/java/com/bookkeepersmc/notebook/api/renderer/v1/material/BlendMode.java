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
package com.bookkeepersmc.notebook.api.renderer.v1.material;

import net.minecraft.client.render.RenderLayer;

public enum BlendMode {
	DEFAULT(null),

	SOLID(RenderLayer.getSolid()),

	CUTOUT_MIPPED(RenderLayer.getCutoutMipped()),

	CUTOUT(RenderLayer.getCutout()),

	TRANSLUCENT(RenderLayer.getTranslucent())
	;

	public final RenderLayer blockRenderLayer;

	BlendMode(RenderLayer blockRenderLayer) {
		this.blockRenderLayer = blockRenderLayer;
	}

	public static BlendMode fromRenderLayer(RenderLayer renderLayer) {
		if (renderLayer == RenderLayer.getSolid()) {
			return SOLID;
		} else if (renderLayer == RenderLayer.getCutoutMipped()) {
			return CUTOUT_MIPPED;
		} else if (renderLayer == RenderLayer.getCutout()) {
			return CUTOUT;
		} else if (renderLayer == RenderLayer.getTranslucent()) {
			return TRANSLUCENT;
		} else {
			return DEFAULT;
		}
	}
}

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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc;

/**
 * Defines the configuration modes for the AoCalculator.
 * This determine the appearance of smooth lighting.
 */
public enum AoConfig {
	/**
	 * Quads will be lit with a slightly modified copy of the vanilla ambient
	 * occlusion calculator. Quads with triangles, non-square or slopes will
	 * not look good in this model.  This model also requires a fixed vertex
	 * winding order for all quads.
	 */
	VANILLA,

	/**
	 * Quads are lit with enhanced lighting logic.  Enhanced lighting will be
	 * similar to vanilla lighting for face-aligned quads, and will be different
	 * (generally better) for triangles, non-square and sloped quads.  Axis-
	 * aligned quads not on the block face will have interpolated brightness based
	 * on depth instead of the all-or-nothing brightness of vanilla.
	 *
	 * <p>Non-vanilla quads can have vertices in any (counter-clockwise) order.
	 */
	ENHANCED,

	/**
	 * Enhanced lighting is configured to mimic vanilla lighting. Results will be
	 * identical to vanilla except that non-square quads, triangles, etc. will
	 * not be sensitive to vertex order.  However shading will not be interpolated
	 * as it is with enhanced. These quads do not occur in vanilla models.
	 * Not recommended for models with complex geometry, but may be faster than
	 * the vanilla calculator when vanilla lighting is desired.
	 */
	EMULATE,

	/**
	 * Quads from vanilla models are lit using {@link #EMULATE} mode and all
	 * other quads are lit using {@link #ENHANCED} mode.  This mode ensures
	 * all vanilla models retain their normal appearance while providing
	 * better lighting for models with more complex geometry.  However,
	 * inconsistencies may be visible when vanilla and non-vanilla models are
	 * near each other.
	 */
	HYBRID;
}

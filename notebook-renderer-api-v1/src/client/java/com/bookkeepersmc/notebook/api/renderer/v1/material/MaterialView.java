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

import com.bookkeepersmc.notebook.api.util.TriState;

/**
 * Getter methods for {@link RenderMaterial} (immutable) and {@link MaterialFinder} (mutable).
 *
 * <p>Values returned by the getters may not necessarily be identical to those requested in the {@link MaterialFinder}.
 * The renderer may choose different values that are sufficiently representative for its own processing.
 */
public interface MaterialView {
	/**
	 * @see MaterialFinder#blendMode(BlendMode)
	 */
	BlendMode blendMode();

	/**
	 * @see MaterialFinder#disableColorIndex(boolean)
	 */
	boolean disableColorIndex();

	/**
	 * @see MaterialFinder#emissive(boolean)
	 */
	boolean emissive();

	/**
	 * @see MaterialFinder#disableDiffuse(boolean)
	 */
	boolean disableDiffuse();

	/**
	 * @see MaterialFinder#ambientOcclusion(TriState)
	 */
	TriState ambientOcclusion();

	/**
	 * @see MaterialFinder#glint(TriState)
	 */
	TriState glint();

	/**
	 * @see MaterialFinder#shadeMode(ShadeMode)
	 *
	 * @apiNote The default implementation will be removed in the next breaking release.
	 */
	default ShadeMode shadeMode() {
		return ShadeMode.ENHANCED;
	}
}

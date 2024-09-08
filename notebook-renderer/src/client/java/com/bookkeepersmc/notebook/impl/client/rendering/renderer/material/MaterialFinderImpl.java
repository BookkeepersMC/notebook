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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer.material;

import java.util.Objects;

import com.bookkeepersmc.notebook.api.renderer.v1.material.BlendMode;
import com.bookkeepersmc.notebook.api.renderer.v1.material.MaterialFinder;
import com.bookkeepersmc.notebook.api.renderer.v1.material.MaterialView;
import com.bookkeepersmc.notebook.api.renderer.v1.material.RenderMaterial;
import com.bookkeepersmc.notebook.api.renderer.v1.material.ShadeMode;
import com.bookkeepersmc.notebook.api.util.TriState;

public class MaterialFinderImpl extends MaterialViewImpl implements MaterialFinder {
	private static int defaultBits = 0;

	static {
		MaterialFinderImpl finder = new MaterialFinderImpl();
		finder.ambientOcclusion(TriState.DEFAULT);
		finder.glint(TriState.DEFAULT);
		defaultBits = finder.bits;

		if (!areBitsValid(defaultBits)) {
			throw new AssertionError("Default MaterialFinder bits are not valid!");
		}
	}

	public MaterialFinderImpl() {
		super(defaultBits);
	}

	@Override
	public MaterialFinder blendMode(BlendMode blendMode) {
		Objects.requireNonNull(blendMode, "BlendMode may not be null");

		bits = (bits & ~BLEND_MODE_MASK) | (blendMode.ordinal() << BLEND_MODE_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder disableColorIndex(boolean disable) {
		bits = disable ? (bits | COLOR_DISABLE_FLAG) : (bits & ~COLOR_DISABLE_FLAG);
		return this;
	}

	@Override
	public MaterialFinder emissive(boolean isEmissive) {
		bits = isEmissive ? (bits | EMISSIVE_FLAG) : (bits & ~EMISSIVE_FLAG);
		return this;
	}

	@Override
	public MaterialFinder disableDiffuse(boolean disable) {
		bits = disable ? (bits | DIFFUSE_FLAG) : (bits & ~DIFFUSE_FLAG);
		return this;
	}

	@Override
	public MaterialFinder ambientOcclusion(TriState mode) {
		Objects.requireNonNull(mode, "ambient occlusion TriState may not be null");

		bits = (bits & ~AO_MASK) | (mode.ordinal() << AO_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder glint(TriState mode) {
		Objects.requireNonNull(mode, "glint TriState may not be null");

		bits = (bits & ~GLINT_MASK) | (mode.ordinal() << GLINT_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder shadeMode(ShadeMode mode) {
		Objects.requireNonNull(mode, "ShadeMode may not be null");

		bits = (bits & ~SHADE_MODE_MASK) | (mode.ordinal() << SHADE_MODE_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder copyFrom(MaterialView material) {
		bits = ((MaterialViewImpl) material).bits;
		return this;
	}

	@Override
	public MaterialFinder clear() {
		bits = defaultBits;
		return this;
	}

	@Override
	public RenderMaterial find() {
		return RenderMaterialImpl.byIndex(bits);
	}
}

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

import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.bitMask;

import net.minecraft.util.math.MathHelper;

import com.bookkeepersmc.notebook.api.renderer.v1.material.BlendMode;
import com.bookkeepersmc.notebook.api.renderer.v1.material.MaterialView;
import com.bookkeepersmc.notebook.api.renderer.v1.material.ShadeMode;
import com.bookkeepersmc.notebook.api.util.TriState;

/**
 * Default implementation of the standard render materials.
 * The underlying representation is simply an int with bit-wise
 * packing of the various material properties. This offers
 * easy/fast interning via int/object hashmap.
 */
public class MaterialViewImpl implements MaterialView {
	private static final BlendMode[] BLEND_MODES = BlendMode.values();
	private static final int BLEND_MODE_COUNT = BLEND_MODES.length;
	private static final TriState[] TRI_STATES = TriState.values();
	private static final int TRI_STATE_COUNT = TRI_STATES.length;
	private static final ShadeMode[] SHADE_MODES = ShadeMode.values();
	private static final int SHADE_MODE_COUNT = SHADE_MODES.length;

	protected static final int BLEND_MODE_BIT_LENGTH = MathHelper.log2DeBruijn(BLEND_MODE_COUNT);
	protected static final int COLOR_DISABLE_BIT_LENGTH = 1;
	protected static final int EMISSIVE_BIT_LENGTH = 1;
	protected static final int DIFFUSE_BIT_LENGTH = 1;
	protected static final int AO_BIT_LENGTH = MathHelper.log2DeBruijn(TRI_STATE_COUNT);
	protected static final int GLINT_BIT_LENGTH = MathHelper.log2DeBruijn(TRI_STATE_COUNT);
	protected static final int SHADE_MODE_BIT_LENGTH = MathHelper.log2DeBruijn(SHADE_MODE_COUNT);

	protected static final int BLEND_MODE_BIT_OFFSET = 0;
	protected static final int COLOR_DISABLE_BIT_OFFSET = BLEND_MODE_BIT_OFFSET + BLEND_MODE_BIT_LENGTH;
	protected static final int EMISSIVE_BIT_OFFSET = COLOR_DISABLE_BIT_OFFSET + COLOR_DISABLE_BIT_LENGTH;
	protected static final int DIFFUSE_BIT_OFFSET = EMISSIVE_BIT_OFFSET + EMISSIVE_BIT_LENGTH;
	protected static final int AO_BIT_OFFSET = DIFFUSE_BIT_OFFSET + DIFFUSE_BIT_LENGTH;
	protected static final int GLINT_BIT_OFFSET = AO_BIT_OFFSET + AO_BIT_LENGTH;
	protected static final int SHADE_MODE_BIT_OFFSET = GLINT_BIT_OFFSET + GLINT_BIT_LENGTH;
	public static final int TOTAL_BIT_LENGTH = SHADE_MODE_BIT_OFFSET + SHADE_MODE_BIT_LENGTH;

	protected static final int BLEND_MODE_MASK = bitMask(BLEND_MODE_BIT_LENGTH, BLEND_MODE_BIT_OFFSET);
	protected static final int COLOR_DISABLE_FLAG = bitMask(COLOR_DISABLE_BIT_LENGTH, COLOR_DISABLE_BIT_OFFSET);
	protected static final int EMISSIVE_FLAG = bitMask(EMISSIVE_BIT_LENGTH, EMISSIVE_BIT_OFFSET);
	protected static final int DIFFUSE_FLAG = bitMask(DIFFUSE_BIT_LENGTH, DIFFUSE_BIT_OFFSET);
	protected static final int AO_MASK = bitMask(AO_BIT_LENGTH, AO_BIT_OFFSET);
	protected static final int GLINT_MASK = bitMask(GLINT_BIT_LENGTH, GLINT_BIT_OFFSET);
	protected static final int SHADE_MODE_MASK = bitMask(SHADE_MODE_BIT_LENGTH, SHADE_MODE_BIT_OFFSET);

	protected static boolean areBitsValid(int bits) {
		int blendMode = (bits & BLEND_MODE_MASK) >>> BLEND_MODE_BIT_OFFSET;
		int ao = (bits & AO_MASK) >>> AO_BIT_OFFSET;
		int glint = (bits & GLINT_MASK) >>> GLINT_BIT_OFFSET;
		int shadeMode = (bits & SHADE_MODE_MASK) >>> SHADE_MODE_BIT_OFFSET;

		return blendMode < BLEND_MODE_COUNT
				&& ao < TRI_STATE_COUNT
				&& glint < TRI_STATE_COUNT
				&& shadeMode < SHADE_MODE_COUNT;
	}

	protected int bits;

	protected MaterialViewImpl(int bits) {
		this.bits = bits;
	}

	@Override
	public BlendMode blendMode() {
		return BLEND_MODES[(bits & BLEND_MODE_MASK) >>> BLEND_MODE_BIT_OFFSET];
	}

	@Override
	public boolean disableColorIndex() {
		return (bits & COLOR_DISABLE_FLAG) != 0;
	}

	@Override
	public boolean emissive() {
		return (bits & EMISSIVE_FLAG) != 0;
	}

	@Override
	public boolean disableDiffuse() {
		return (bits & DIFFUSE_FLAG) != 0;
	}

	@Override
	public TriState ambientOcclusion() {
		return TRI_STATES[(bits & AO_MASK) >>> AO_BIT_OFFSET];
	}

	@Override
	public TriState glint() {
		return TRI_STATES[(bits & GLINT_MASK) >>> GLINT_BIT_OFFSET];
	}

	@Override
	public ShadeMode shadeMode() {
		return SHADE_MODES[(bits & SHADE_MODE_MASK) >>> SHADE_MODE_BIT_OFFSET];
	}
}

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

import com.bookkeepersmc.notebook.api.renderer.v1.material.RenderMaterial;

public class RenderMaterialImpl extends MaterialViewImpl implements RenderMaterial {
	public static final int VALUE_COUNT = 1 << TOTAL_BIT_LENGTH;
	private static final RenderMaterialImpl[] BY_INDEX = new RenderMaterialImpl[VALUE_COUNT];

	static {
		for (int i = 0; i < VALUE_COUNT; i++) {
			if (areBitsValid(i)) {
				BY_INDEX[i] = new RenderMaterialImpl(i);
			}
		}
	}

	private RenderMaterialImpl(int bits) {
		super(bits);
	}

	public int index() {
		return bits;
	}

	public static RenderMaterialImpl byIndex(int index) {
		return BY_INDEX[index];
	}

	public static RenderMaterialImpl setDisableDiffuse(RenderMaterialImpl material, boolean disable) {
		if (material.disableDiffuse() != disable) {
			return byIndex(disable ? (material.bits | DIFFUSE_FLAG) : (material.bits & ~DIFFUSE_FLAG));
		}

		return material;
	}
}

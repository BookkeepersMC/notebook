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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh;

import com.bookkeepersmc.notebook.api.renderer.v1.mesh.Mesh;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.MeshBuilder;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadEmitter;

/**
 * Our implementation of {@link MeshBuilder}, used for static mesh creation and baking.
 * Not much to it - mainly it just needs to grow the int[] array as quads are appended
 * and maintain/provide a properly-configured {@link com.bookkeepersmc.notebook.api.renderer.v1.mesh.MutableQuadView} instance.
 * All the encoding and other work is handled in the quad base classes.
 * The one interesting bit is in {@link Maker#emitDirectly()}.
 */
public class MeshBuilderImpl implements MeshBuilder {
	private int[] data = new int[256];
	private int index = 0;
	private int limit = data.length;
	private final Maker maker = new Maker();

	public MeshBuilderImpl() {
		ensureCapacity(EncodingFormat.TOTAL_STRIDE);
		maker.data = data;
		maker.baseIndex = index;
		maker.clear();
	}

	protected void ensureCapacity(int stride) {
		if (stride > limit - index) {
			limit *= 2;
			final int[] bigger = new int[limit];
			System.arraycopy(data, 0, bigger, 0, index);
			data = bigger;
			maker.data = data;
		}
	}

	@Override
	public QuadEmitter getEmitter() {
		maker.clear();
		return maker;
	}

	@Override
	public Mesh build() {
		final int[] packed = new int[index];
		System.arraycopy(data, 0, packed, 0, index);
		index = 0;
		maker.baseIndex = index;
		maker.clear();
		return new MeshImpl(packed);
	}

	/**
	 * Our base classes are used differently so we define final
	 * encoding steps in subtypes. This will be a static mesh used
	 * at render time so we want to capture all geometry now and
	 * apply non-location-dependent lighting.
	 */
	private class Maker extends MutableQuadViewImpl {
		@Override
		public void emitDirectly() {
			computeGeometry();
			index += EncodingFormat.TOTAL_STRIDE;
			ensureCapacity(EncodingFormat.TOTAL_STRIDE);
			baseIndex = index;
		}
	}
}

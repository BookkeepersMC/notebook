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

import java.util.function.Consumer;

import com.bookkeepersmc.notebook.api.renderer.v1.mesh.Mesh;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadEmitter;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadView;

/**
 * Implementation of {@link Mesh}.
 * The way we encode meshes makes it very simple.
 */
public class MeshImpl implements Mesh {
	/** Used to satisfy external calls to {@link #forEach(Consumer)}. */
	private final ThreadLocal<QuadViewImpl> cursorPool = ThreadLocal.withInitial(QuadViewImpl::new);

	final int[] data;

	MeshImpl(int[] data) {
		this.data = data;
	}

	@Override
	public void forEach(Consumer<QuadView> consumer) {
		forEach(consumer, cursorPool.get());
	}

	/**
	 * The renderer can call this with its own cursor
	 * to avoid the performance hit of a thread-local lookup.
	 * Also means renderer can hold final references to quad buffers.
	 */
	void forEach(Consumer<QuadView> consumer, QuadViewImpl cursor) {
		final int limit = data.length;
		int index = 0;
		cursor.data = this.data;

		while (index < limit) {
			cursor.baseIndex = index;
			cursor.load();
			consumer.accept(cursor);
			index += EncodingFormat.TOTAL_STRIDE;
		}
	}

	@Override
	public void outputTo(QuadEmitter emitter) {
		MutableQuadViewImpl e = (MutableQuadViewImpl) emitter;
		final int[] data = this.data;
		final int limit = data.length;
		int index = 0;

		while (index < limit) {
			System.arraycopy(data, index, e.data, e.baseIndex, EncodingFormat.TOTAL_STRIDE);
			e.load();
			e.emitDirectly();
			index += EncodingFormat.TOTAL_STRIDE;
		}

		e.clear();
	}
}

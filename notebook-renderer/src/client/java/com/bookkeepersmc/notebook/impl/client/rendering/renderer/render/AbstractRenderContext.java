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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer.render;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.bookkeepersmc.notebook.api.renderer.v1.mesh.Mesh;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.MutableQuadView;
import com.bookkeepersmc.notebook.api.renderer.v1.render.RenderContext;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.MutableQuadViewImpl;

abstract class AbstractRenderContext implements RenderContext {
	private static final QuadTransform NO_TRANSFORM = q -> true;

	private QuadTransform activeTransform = NO_TRANSFORM;
	private final ObjectArrayList<QuadTransform> transformStack = new ObjectArrayList<>();
	private final QuadTransform stackTransform = q -> {
		int i = transformStack.size() - 1;

		while (i >= 0) {
			if (!transformStack.get(i--).transform(q)) {
				return false;
			}
		}

		return true;
	};

	@Deprecated
	private final Consumer<Mesh> meshConsumer = mesh -> mesh.outputTo(getEmitter());

	protected Matrix4f matrix;
	protected Matrix3f normalMatrix;
	protected int overlay;
	private final Vector4f posVec = new Vector4f();
	private final Vector3f normalVec = new Vector3f();

	protected final boolean transform(MutableQuadView q) {
		return activeTransform.transform(q);
	}

	@Override
	public boolean hasTransform() {
		return activeTransform != NO_TRANSFORM;
	}

	@Override
	public void pushTransform(QuadTransform transform) {
		if (transform == null) {
			throw new NullPointerException("Renderer received null QuadTransform.");
		}

		transformStack.push(transform);

		if (transformStack.size() == 1) {
			activeTransform = transform;
		} else if (transformStack.size() == 2) {
			activeTransform = stackTransform;
		}
	}

	@Override
	public void popTransform() {
		transformStack.pop();

		if (transformStack.size() == 0) {
			activeTransform = NO_TRANSFORM;
		} else if (transformStack.size() == 1) {
			activeTransform = transformStack.get(0);
		}
	}

	// Overridden to prevent allocating a lambda every time this method is called.
	@Deprecated
	@Override
	public Consumer<Mesh> meshConsumer() {
		return meshConsumer;
	}

	/** final output step, common to all renders. */
	protected void bufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer) {
		final Vector4f posVec = this.posVec;
		final Vector3f normalVec = this.normalVec;
		final boolean useNormals = quad.hasVertexNormals();

		if (useNormals) {
			quad.populateMissingNormals();
		} else {
			normalVec.set(quad.faceNormal());
			normalVec.mul(normalMatrix);
		}

		for (int i = 0; i < 4; i++) {
			posVec.set(quad.x(i), quad.y(i), quad.z(i), 1.0f);
			posVec.mul(matrix);
			vertexConsumer.xyz(posVec.x(), posVec.y(), posVec.z());

			final int color = quad.color(i);
			vertexConsumer.color((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF);
			vertexConsumer.uv0(quad.u(i), quad.v(i));
			vertexConsumer.uv1(overlay);
			vertexConsumer.uv2(quad.lightmap(i));

			if (useNormals) {
				quad.copyNormal(i, normalVec);
				normalVec.mul(normalMatrix);
			}

			vertexConsumer.normal(normalVec.x(), normalVec.y(), normalVec.z());
		}
	}
}

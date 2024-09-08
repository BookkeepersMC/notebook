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

import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.EMPTY;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.HEADER_BITS;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.HEADER_COLOR_INDEX;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.HEADER_STRIDE;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.HEADER_TAG;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.VERTEX_COLOR;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.VERTEX_LIGHTMAP;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.VERTEX_NORMAL;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.VERTEX_STRIDE;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.VERTEX_U;
import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat.VERTEX_X;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import com.bookkeepersmc.notebook.api.renderer.v1.material.RenderMaterial;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadEmitter;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadView;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.NotebookRenderer;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.helper.ColorHelper;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.helper.NormalHelper;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.helper.TextureHelper;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.material.RenderMaterialImpl;

/**
 * Almost-concrete implementation of a mutable quad. The only missing part is {@link #emitDirectly()},
 * because that depends on where/how it is used. (Mesh encoding vs. render-time transformation).
 *
 * <p>In many cases an instance of this class is used as an "editor quad". The editor quad's
 * {@link #emitDirectly()} method calls some other internal method that transforms the quad
 * data and then buffers it. Transformations should be the same as they would be in a vanilla
 * render - the editor is serving mainly as a way to access vertex data without magical
 * numbers. It also allows for a consistent interface for those transformations.
 */
public abstract class MutableQuadViewImpl extends QuadViewImpl implements QuadEmitter {
	public void clear() {
		System.arraycopy(EMPTY, 0, data, baseIndex, EncodingFormat.TOTAL_STRIDE);
		isGeometryInvalid = true;
		nominalFace = null;
		normalFlags(0);
		tag(0);
		colorIndex(-1);
		cullFace(null);
		material(NotebookRenderer.MATERIAL_STANDARD);
	}

	@Override
	public MutableQuadViewImpl pos(int vertexIndex, float x, float y, float z) {
		final int index = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X;
		data[index] = Float.floatToRawIntBits(x);
		data[index + 1] = Float.floatToRawIntBits(y);
		data[index + 2] = Float.floatToRawIntBits(z);
		isGeometryInvalid = true;
		return this;
	}

	@Override
	public MutableQuadViewImpl color(int vertexIndex, int color) {
		data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_COLOR] = color;
		return this;
	}

	@Override
	public MutableQuadViewImpl uv(int vertexIndex, float u, float v) {
		final int i = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_U;
		data[i] = Float.floatToRawIntBits(u);
		data[i + 1] = Float.floatToRawIntBits(v);
		return this;
	}

	@Override
	public MutableQuadViewImpl spriteBake(Sprite sprite, int bakeFlags) {
		TextureHelper.bakeSprite(this, sprite, bakeFlags);
		return this;
	}

	@Override
	public MutableQuadViewImpl lightmap(int vertexIndex, int lightmap) {
		data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_LIGHTMAP] = lightmap;
		return this;
	}

	protected void normalFlags(int flags) {
		data[baseIndex + HEADER_BITS] = EncodingFormat.normalFlags(data[baseIndex + HEADER_BITS], flags);
	}

	@Override
	public MutableQuadViewImpl normal(int vertexIndex, float x, float y, float z) {
		normalFlags(normalFlags() | (1 << vertexIndex));
		data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_NORMAL] = NormalHelper.packNormal(x, y, z);
		return this;
	}

	/**
	 * Internal helper method. Copies face normals to vertex normals lacking one.
	 */
	public final void populateMissingNormals() {
		final int normalFlags = this.normalFlags();

		if (normalFlags == 0b1111) return;

		final int packedFaceNormal = packedFaceNormal();

		for (int v = 0; v < 4; v++) {
			if ((normalFlags & (1 << v)) == 0) {
				data[baseIndex + v * VERTEX_STRIDE + VERTEX_NORMAL] = packedFaceNormal;
			}
		}

		normalFlags(0b1111);
	}

	@Override
	public final MutableQuadViewImpl cullFace(@Nullable Direction face) {
		data[baseIndex + HEADER_BITS] = EncodingFormat.cullFace(data[baseIndex + HEADER_BITS], face);
		nominalFace(face);
		return this;
	}

	@Override
	public final MutableQuadViewImpl nominalFace(@Nullable Direction face) {
		nominalFace = face;
		return this;
	}

	@Override
	public final MutableQuadViewImpl material(RenderMaterial material) {
		if (material == null) {
			material = NotebookRenderer.MATERIAL_STANDARD;
		}

		data[baseIndex + HEADER_BITS] = EncodingFormat.material(data[baseIndex + HEADER_BITS], (RenderMaterialImpl) material);
		return this;
	}

	@Override
	public final MutableQuadViewImpl colorIndex(int colorIndex) {
		data[baseIndex + HEADER_COLOR_INDEX] = colorIndex;
		return this;
	}

	@Override
	public final MutableQuadViewImpl tag(int tag) {
		data[baseIndex + HEADER_TAG] = tag;
		return this;
	}

	@Override
	public MutableQuadViewImpl copyFrom(QuadView quad) {
		final QuadViewImpl q = (QuadViewImpl) quad;
		q.computeGeometry();

		System.arraycopy(q.data, q.baseIndex, data, baseIndex, EncodingFormat.TOTAL_STRIDE);
		faceNormal.set(q.faceNormal);
		nominalFace = q.nominalFace;
		isGeometryInvalid = false;
		return this;
	}

	@Override
	public final MutableQuadViewImpl fromVanilla(int[] quadData, int startIndex) {
		System.arraycopy(quadData, startIndex, data, baseIndex + HEADER_STRIDE, VANILLA_QUAD_STRIDE);
		isGeometryInvalid = true;

		int colorIndex = baseIndex + VERTEX_COLOR;

		for (int i = 0; i < 4; i++) {
			data[colorIndex] = ColorHelper.fromVanillaColor(data[colorIndex]);
			colorIndex += VERTEX_STRIDE;
		}

		return this;
	}

	@Override
	public final MutableQuadViewImpl fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace) {
		fromVanilla(quad.getVertexData(), 0);
		data[baseIndex + HEADER_BITS] = EncodingFormat.cullFace(0, cullFace);
		nominalFace(quad.getFace());
		colorIndex(quad.getColorIndex());

		if (!quad.hasShade()) {
			material = RenderMaterialImpl.setDisableDiffuse((RenderMaterialImpl) material, true);
		}

		int lightEmission = quad.method_62324();

		if (lightEmission > 0) {
			for (int i = 0; i < 4; i++) {
				lightmap(i, LightmapTextureManager.getPackedCoordsWithEmission(lightmap(i), lightEmission));
			}
		}

		material(material);
		tag(0);
		return this;
	}

	/**
	 * Emit the quad without clearing the underlying data.
	 * Geometry is not guaranteed to be valid when called, but can be computed by calling {@link #computeGeometry()}.
	 */
	public abstract void emitDirectly();

	@Override
	public final MutableQuadViewImpl emit() {
		emitDirectly();
		clear();
		return this;
	}
}

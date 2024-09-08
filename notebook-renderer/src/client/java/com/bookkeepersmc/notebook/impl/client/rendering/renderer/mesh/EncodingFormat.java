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

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadView;
import com.bookkeepersmc.notebook.api.renderer.v1.model.ModelHelper;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.helper.GeometryHelper;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.material.MaterialViewImpl;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.material.RenderMaterialImpl;

/**
 * Holds all the array offsets and bit-wise encoders/decoders for
 * packing/unpacking quad data in an array of integers.
 * All of this is implementation-specific - that's why it isn't a "helper" class.
 */
public abstract class EncodingFormat {
	private EncodingFormat() { }

	static final int HEADER_BITS = 0;
	static final int HEADER_FACE_NORMAL = 1;
	static final int HEADER_COLOR_INDEX = 2;
	static final int HEADER_TAG = 3;
	public static final int HEADER_STRIDE = 4;

	static final int VERTEX_X;
	static final int VERTEX_Y;
	static final int VERTEX_Z;
	static final int VERTEX_COLOR;
	static final int VERTEX_U;
	static final int VERTEX_V;
	static final int VERTEX_LIGHTMAP;
	static final int VERTEX_NORMAL;
	public static final int VERTEX_STRIDE;

	public static final int QUAD_STRIDE;
	public static final int QUAD_STRIDE_BYTES;
	public static final int TOTAL_STRIDE;

	static {
		final VertexFormat format = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
		VERTEX_X = HEADER_STRIDE + 0;
		VERTEX_Y = HEADER_STRIDE + 1;
		VERTEX_Z = HEADER_STRIDE + 2;
		VERTEX_COLOR = HEADER_STRIDE + 3;
		VERTEX_U = HEADER_STRIDE + 4;
		VERTEX_V = VERTEX_U + 1;
		VERTEX_LIGHTMAP = HEADER_STRIDE + 6;
		VERTEX_NORMAL = HEADER_STRIDE + 7;
		VERTEX_STRIDE = format.getVertexSize() / 4;
		QUAD_STRIDE = VERTEX_STRIDE * 4;
		QUAD_STRIDE_BYTES = QUAD_STRIDE * 4;
		TOTAL_STRIDE = HEADER_STRIDE + QUAD_STRIDE;

		Preconditions.checkState(VERTEX_STRIDE == QuadView.VANILLA_VERTEX_STRIDE, "Indigo vertex stride (%s) mismatched with rendering API (%s)", VERTEX_STRIDE, QuadView.VANILLA_VERTEX_STRIDE);
		Preconditions.checkState(QUAD_STRIDE == QuadView.VANILLA_QUAD_STRIDE, "Indigo quad stride (%s) mismatched with rendering API (%s)", QUAD_STRIDE, QuadView.VANILLA_QUAD_STRIDE);
	}

	/** used for quick clearing of quad buffers. */
	static final int[] EMPTY = new int[TOTAL_STRIDE];

	private static final int DIRECTION_COUNT = Direction.values().length;
	private static final int NULLABLE_DIRECTION_COUNT = DIRECTION_COUNT + 1;

	private static final int CULL_BIT_LENGTH = MathHelper.log2DeBruijn(NULLABLE_DIRECTION_COUNT);
	private static final int LIGHT_BIT_LENGTH = MathHelper.log2DeBruijn(DIRECTION_COUNT);
	private static final int NORMALS_BIT_LENGTH = 4;
	private static final int GEOMETRY_BIT_LENGTH = GeometryHelper.FLAG_BIT_COUNT;
	private static final int MATERIAL_BIT_LENGTH = MaterialViewImpl.TOTAL_BIT_LENGTH;

	private static final int CULL_BIT_OFFSET = 0;
	private static final int LIGHT_BIT_OFFSET = CULL_BIT_OFFSET + CULL_BIT_LENGTH;
	private static final int NORMALS_BIT_OFFSET = LIGHT_BIT_OFFSET + LIGHT_BIT_LENGTH;
	private static final int GEOMETRY_BIT_OFFSET = NORMALS_BIT_OFFSET + NORMALS_BIT_LENGTH;
	private static final int MATERIAL_BIT_OFFSET = GEOMETRY_BIT_OFFSET + GEOMETRY_BIT_LENGTH;
	private static final int TOTAL_BIT_LENGTH = MATERIAL_BIT_OFFSET + MATERIAL_BIT_LENGTH;

	private static final int CULL_MASK = bitMask(CULL_BIT_LENGTH, CULL_BIT_OFFSET);
	private static final int LIGHT_MASK = bitMask(LIGHT_BIT_LENGTH, LIGHT_BIT_OFFSET);
	private static final int NORMALS_MASK = bitMask(NORMALS_BIT_LENGTH, NORMALS_BIT_OFFSET);
	private static final int GEOMETRY_MASK = bitMask(GEOMETRY_BIT_LENGTH, GEOMETRY_BIT_OFFSET);
	private static final int MATERIAL_MASK = bitMask(MATERIAL_BIT_LENGTH, MATERIAL_BIT_OFFSET);

	static {
		Preconditions.checkArgument(TOTAL_BIT_LENGTH <= 32, "Indigo header encoding bit count (%s) exceeds integer bit length)", TOTAL_STRIDE);
	}

	public static int bitMask(int bitLength, int bitOffset) {
		return ((1 << bitLength) - 1) << bitOffset;
	}

	@Nullable
	static Direction cullFace(int bits) {
		return ModelHelper.faceFromIndex((bits & CULL_MASK) >>> CULL_BIT_OFFSET);
	}

	static int cullFace(int bits, @Nullable Direction face) {
		return (bits & ~CULL_MASK) | (ModelHelper.toFaceIndex(face) << CULL_BIT_OFFSET);
	}

	static Direction lightFace(int bits) {
		return ModelHelper.faceFromIndex((bits & LIGHT_MASK) >>> LIGHT_BIT_OFFSET);
	}

	static int lightFace(int bits, Direction face) {
		return (bits & ~LIGHT_MASK) | (ModelHelper.toFaceIndex(face) << LIGHT_BIT_OFFSET);
	}

	/** indicate if vertex normal has been set - bits correspond to vertex ordinals. */
	static int normalFlags(int bits) {
		return (bits & NORMALS_MASK) >>> NORMALS_BIT_OFFSET;
	}

	static int normalFlags(int bits, int normalFlags) {
		return (bits & ~NORMALS_MASK) | ((normalFlags << NORMALS_BIT_OFFSET) & NORMALS_MASK);
	}

	static int geometryFlags(int bits) {
		return (bits & GEOMETRY_MASK) >>> GEOMETRY_BIT_OFFSET;
	}

	static int geometryFlags(int bits, int geometryFlags) {
		return (bits & ~GEOMETRY_MASK) | ((geometryFlags << GEOMETRY_BIT_OFFSET) & GEOMETRY_MASK);
	}

	static RenderMaterialImpl material(int bits) {
		return RenderMaterialImpl.byIndex((bits & MATERIAL_MASK) >>> MATERIAL_BIT_OFFSET);
	}

	static int material(int bits, RenderMaterialImpl material) {
		return (bits & ~MATERIAL_MASK) | (material.index() << MATERIAL_BIT_OFFSET);
	}
}

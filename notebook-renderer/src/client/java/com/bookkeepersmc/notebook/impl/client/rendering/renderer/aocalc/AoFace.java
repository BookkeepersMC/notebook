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

import static com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc.AoVertexClampFunction.CLAMP_FUNC;
import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

import com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.QuadViewImpl;

/**
 * Adapted from vanilla BlockModelRenderer.AoCalculator.
 */
enum AoFace {
	AOF_DOWN(new Direction[] { WEST, EAST, NORTH, SOUTH }, (q, i) -> CLAMP_FUNC.clamp(q.y(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.x(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = (1 - u) * v;
		w[1] = (1 - u) * (1 - v);
		w[2] = u * (1 - v);
		w[3] = u * v;
	}),
	AOF_UP(new Direction[] { EAST, WEST, NORTH, SOUTH }, (q, i) -> 1 - CLAMP_FUNC.clamp(q.y(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.x(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = u * v;
		w[1] = u * (1 - v);
		w[2] = (1 - u) * (1 - v);
		w[3] = (1 - u) * v;
	}),
	AOF_NORTH(new Direction[] { UP, DOWN, EAST, WEST }, (q, i) -> CLAMP_FUNC.clamp(q.z(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.x(i));
		w[0] = u * (1 - v);
		w[1] = u * v;
		w[2] = (1 - u) * v;
		w[3] = (1 - u) * (1 - v);
	}),
	AOF_SOUTH(new Direction[] { WEST, EAST, DOWN, UP }, (q, i) -> 1 - CLAMP_FUNC.clamp(q.z(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.x(i));
		w[0] = u * (1 - v);
		w[1] = (1 - u) * (1 - v);
		w[2] = (1 - u) * v;
		w[3] = u * v;
	}),
	AOF_WEST(new Direction[] { UP, DOWN, NORTH, SOUTH }, (q, i) -> CLAMP_FUNC.clamp(q.x(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = u * v;
		w[1] = u * (1 - v);
		w[2] = (1 - u) * (1 - v);
		w[3] = (1 - u) * v;
	}),
	AOF_EAST(new Direction[] { DOWN, UP, NORTH, SOUTH }, (q, i) -> 1 - CLAMP_FUNC.clamp(q.x(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = (1 - u) * v;
		w[1] = (1 - u) * (1 - v);
		w[2] = u * (1 - v);
		w[3] = u * v;
	});

	final Direction[] neighbors;
	final WeightFunction weightFunc;
	final Vertex2Float depthFunc;

	AoFace(Direction[] faces, Vertex2Float depthFunc, WeightFunction weightFunc) {
		this.neighbors = faces;
		this.depthFunc = depthFunc;
		this.weightFunc = weightFunc;
	}

	private static final AoFace[] values = Util.make(new AoFace[6], (neighborData) -> {
		neighborData[DOWN.getId()] = AOF_DOWN;
		neighborData[UP.getId()] = AOF_UP;
		neighborData[NORTH.getId()] = AOF_NORTH;
		neighborData[SOUTH.getId()] = AOF_SOUTH;
		neighborData[WEST.getId()] = AOF_WEST;
		neighborData[EAST.getId()] = AOF_EAST;
	});

	public static AoFace get(Direction direction) {
		return values[direction.getId()];
	}

	/**
	 * Implementations handle bilinear interpolation of a point on a light face
	 * by computing weights for each corner of the light face. Relies on the fact
	 * that each face is a unit cube. Uses coordinates from axes orthogonal to face
	 * as distance from the edge of the cube, flipping as needed. Multiplying distance
	 * coordinate pairs together gives sub-area that are the corner weights.
	 * Weights sum to 1 because it is a unit cube. Values are stored in the provided array.
	 */
	@FunctionalInterface
	interface WeightFunction {
		void apply(QuadViewImpl q, int vertexIndex, float[] out);
	}

	@FunctionalInterface
	interface Vertex2Float {
		float apply(QuadViewImpl q, int vertexIndex);
	}
}

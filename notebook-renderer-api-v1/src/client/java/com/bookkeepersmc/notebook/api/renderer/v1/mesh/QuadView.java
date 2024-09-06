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
package com.bookkeepersmc.notebook.api.renderer.v1.mesh;

import com.mojang.blaze3d.vertex.VertexFormats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import com.bookkeepersmc.notebook.api.renderer.v1.material.RenderMaterial;

public interface QuadView {
	/** Count of integers in a conventional (un-modded) block or item vertex. */
	int VANILLA_VERTEX_STRIDE = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize() / 4;

	/** Count of integers in a conventional (un-modded) block or item quad. */
	int VANILLA_QUAD_STRIDE = VANILLA_VERTEX_STRIDE * 4;

	/**
	 * Retrieve geometric position, x coordinate.
	 */
	float x(int vertexIndex);

	/**
	 * Retrieve geometric position, y coordinate.
	 */
	float y(int vertexIndex);

	/**
	 * Retrieve geometric position, z coordinate.
	 */
	float z(int vertexIndex);

	/**
	 * Convenience: access x, y, z by index 0-2.
	 */
	float posByIndex(int vertexIndex, int coordinateIndex);

	/**
	 * Pass a non-null target to avoid allocation - will be returned with values.
	 * Otherwise returns a new instance.
	 */
	Vector3f copyPos(int vertexIndex, @Nullable Vector3f target);

	/**
	 * Retrieve vertex color in ARGB format (0xAARRGGBB).
	 */
	int color(int vertexIndex);

	/**
	 * Retrieve horizontal texture coordinates.
	 */
	float u(int vertexIndex);

	/**
	 * Retrieve vertical texture coordinates.
	 */
	float v(int vertexIndex);

	/**
	 * Pass a non-null target to avoid allocation - will be returned with values.
	 * Otherwise returns a new instance.
	 */
	Vector2f copyUv(int vertexIndex, @Nullable Vector2f target);

	/**
	 * Minimum block brightness. Zero if not set.
	 */
	int lightmap(int vertexIndex);

	/**
	 * If false, no vertex normal was provided.
	 * Lighting should use face normal in that case.
	 */
	boolean hasNormal(int vertexIndex);

	/**
	 * Will return {@link Float#NaN} if normal not present.
	 */
	float normalX(int vertexIndex);

	/**
	 * Will return {@link Float#NaN} if normal not present.
	 */
	float normalY(int vertexIndex);

	/**
	 * Will return {@link Float#NaN} if normal not present.
	 */
	float normalZ(int vertexIndex);

	/**
	 * Pass a non-null target to avoid allocation - will be returned with values.
	 * Otherwise returns a new instance. Returns null if normal not present.
	 */
	@Nullable
	Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target);

	/**
	 * If non-null, quad should not be rendered in-world if the
	 * opposite face of a neighbor block occludes it.
	 *
	 * @see MutableQuadView#cullFace(Direction)
	 */
	@Nullable
	Direction cullFace();

	/**
	 * Equivalent to {@link BakedQuad#getFace()}. This is the face used for vanilla lighting
	 * calculations and will be the block face to which the quad is most closely aligned. Always
	 * the same as cull face for quads that are on a block face, but never null.
	 */
	@NotNull
	Direction lightFace();

	/**
	 * See {@link MutableQuadView#nominalFace(Direction)}.
	 */
	@Nullable
	Direction nominalFace();

	/**
	 * Normal of the quad as implied by geometry. Will be invalid
	 * if quad vertices are not co-planar. Typically computed lazily
	 * on demand.
	 *
	 * <p>Not typically needed by models. Exposed to enable standard lighting
	 * utility functions for use by renderers.
	 */
	Vector3f faceNormal();

	/**
	 * Retrieves the material serialized with the quad.
	 */
	RenderMaterial material();

	/**
	 * Retrieves the quad color index serialized with the quad.
	 */
	int colorIndex();

	/**
	 * Retrieves the integer tag encoded with this quad via {@link MutableQuadView#tag(int)}.
	 * Will return zero if no tag was set. For use by models.
	 */
	int tag();

	/**
	 * Reads baked vertex data and outputs standard {@link BakedQuad#getVertexData() baked quad vertex data}
	 * in the given array and location.
	 *
	 * @param target Target array for the baked quad data.
	 *
	 * @param targetIndex Starting position in target array - array must have
	 * at least {@link #VANILLA_QUAD_STRIDE} elements available at this index.
	 */
	void toVanilla(int[] target, int targetIndex);

	/**
	 * Generates a new BakedQuad instance with texture
	 * coordinates and colors from the given sprite.
	 *
	 * @param sprite {@link MutableQuadView} does not serialize sprites
	 * so the sprite must be provided by the caller.
	 *
	 * @return A new baked quad instance with the closest-available appearance
	 * supported by vanilla features. Will retain emissive light maps, for example,
	 * but the standard Minecraft renderer will not use them.
	 */
	default BakedQuad toBakedQuad(Sprite sprite) {
		int[] vertexData = new int[VANILLA_QUAD_STRIDE];
		toVanilla(vertexData, 0);

		// Mimic material properties to the largest possible extent
		int outputColorIndex = material().disableColorIndex() ? -1 : colorIndex();
		boolean outputShade = !material().disableDiffuse();
		// The output light emission is equal to the minimum of all four sky light values and all four block light values.
		int outputLightEmission = 15;

		for (int i = 0; i < 4; i++) {
			int lightmap = lightmap(i);

			if (lightmap == 0) {
				outputLightEmission = 0;
				break;
			}

			int blockLight = LightmapTextureManager.getBlockLightCoordinates(lightmap);
			int skyLight = LightmapTextureManager.getSkyLightCoordinates(lightmap);
			outputLightEmission = Math.min(outputLightEmission, Math.min(blockLight, skyLight));
		}

		return new BakedQuad(vertexData, outputColorIndex, lightFace(), sprite, outputShade, outputLightEmission);
	}
}

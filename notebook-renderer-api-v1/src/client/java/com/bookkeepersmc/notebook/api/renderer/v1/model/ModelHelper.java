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
package com.bookkeepersmc.notebook.api.renderer.v1.model;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import com.bookkeepersmc.notebook.api.renderer.v1.mesh.Mesh;

/**
 * Collection of utilities for model implementations.
 */
public final class ModelHelper {
	private ModelHelper() {
	}

	/** Result from {@link #toFaceIndex(Direction)} for null values. */
	public static final int NULL_FACE_ID = 6;

	/**
	 * Convenient way to encode faces that may be null.
	 * Null is returned as {@link #NULL_FACE_ID}.
	 * Use {@link #faceFromIndex(int)} to retrieve encoded face.
	 */
	public static int toFaceIndex(@Nullable Direction face) {
		return face == null ? NULL_FACE_ID : face.getId();
	}

	/**
	 * Use to decode a result from {@link #toFaceIndex(Direction)}.
	 * Return value will be null if encoded value was null.
	 * Can also be used for no-allocation iteration of {@link Direction#values()},
	 * optionally including the null face. (Use &lt; or  &lt;= {@link #NULL_FACE_ID}
	 * to exclude or include the null value, respectively.)
	 */
	@Nullable
	public static Direction faceFromIndex(int faceIndex) {
		return FACES[faceIndex];
	}

	/** @see #faceFromIndex(int) */
	private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);

	/**
	 * Converts a mesh into an array of lists of vanilla baked quads.
	 * Useful for creating vanilla baked models when required for compatibility.
	 * The array indexes correspond to {@link Direction#getId()} with the
	 * addition of {@link #NULL_FACE_ID}.
	 *
	 * <p>Retrieves sprites from the block texture atlas via {@link SpriteFinder}.
	 */
	public static List<BakedQuad>[] toQuadLists(Mesh mesh) {
		SpriteFinder finder = SpriteFinder.get(Minecraft.getInstance().getBakedModelManager().getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));

		@SuppressWarnings("unchecked")
		final ImmutableList.Builder<BakedQuad>[] builders = new ImmutableList.Builder[7];

		for (int i = 0; i < 7; i++) {
			builders[i] = ImmutableList.builder();
		}

		if (mesh != null) {
			mesh.forEach(q -> {
				Direction cullFace = q.cullFace();
				builders[cullFace == null ? NULL_FACE_ID : cullFace.getId()].add(q.toBakedQuad(finder.find(q)));
			});
		}

		@SuppressWarnings("unchecked")
		List<BakedQuad>[] result = new List[7];

		for (int i = 0; i < 7; i++) {
			result[i] = builders[i].build();
		}

		return result;
	}

	/**
	 * The vanilla model transformation logic is closely coupled with model deserialization.
	 * That does little good for modded model loaders and procedurally generated models.
	 * This convenient construction method applies the same scaling factors used for vanilla models.
	 * This means you can use values from a vanilla JSON file as inputs to this method.
	 */
	private static Transformation makeTransform(float rotationX, float rotationY, float rotationZ, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float scaleZ) {
		Vector3f translation = new Vector3f(translationX, translationY, translationZ);
		translation.mul(0.0625f);
		translation.set(MathHelper.clamp(translation.x, -5.0F, 5.0F), MathHelper.clamp(translation.y, -5.0F, 5.0F), MathHelper.clamp(translation.z, -5.0F, 5.0F));
		return new Transformation(new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
	}

	public static final Transformation TRANSFORM_BLOCK_GUI = makeTransform(30, 225, 0, 0, 0, 0, 0.625f, 0.625f, 0.625f);
	public static final Transformation TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 3, 0, 0.25f, 0.25f, 0.25f);
	public static final Transformation TRANSFORM_BLOCK_FIXED = makeTransform(0, 0, 0, 0, 0, 0, 0.5f, 0.5f, 0.5f);
	public static final Transformation TRANSFORM_BLOCK_3RD_PERSON_RIGHT = makeTransform(75, 45, 0, 0, 2.5f, 0, 0.375f, 0.375f, 0.375f);
	public static final Transformation TRANSFORM_BLOCK_1ST_PERSON_RIGHT = makeTransform(0, 45, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);
	public static final Transformation TRANSFORM_BLOCK_1ST_PERSON_LEFT = makeTransform(0, 225, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);

	/**
	 * Mimics the vanilla model transformation used for most vanilla blocks,
	 * and should be suitable for most custom block-like models.
	 */
	public static final ModelTransformation MODEL_TRANSFORMATION_BLOCK = new ModelTransformation(TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_1ST_PERSON_LEFT, TRANSFORM_BLOCK_1ST_PERSON_RIGHT, Transformation.IDENTITY, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED);
}

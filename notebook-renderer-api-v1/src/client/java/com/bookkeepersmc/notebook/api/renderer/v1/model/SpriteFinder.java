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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;

import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadView;
import com.bookkeepersmc.notebook.impl.renderer.SpriteFinderImpl;

/**
 * Indexes a texture atlas to allow fast lookup of Sprites from
 * baked vertex coordinates.  Main use is for {@link com.bookkeepersmc.notebook.api.renderer.v1.mesh.Mesh}-based models
 * to generate vanilla quads on demand without tracking and retaining
 * the sprites that were baked into the mesh. In other words, this class
 * supplies the sprite parameter for {@link QuadView#toBakedQuad(Sprite)}.
 */
@ApiStatus.NonExtendable
public interface SpriteFinder {
	/**
	 * Retrieves or creates the finder for the given atlas.
	 * Instances should not be retained as fields, or they must be
	 * refreshed whenever there is a resource reload or other event
	 * that causes atlas textures to be re-stitched.
	 */
	static SpriteFinder get(SpriteAtlasTexture atlas) {
		return SpriteFinderImpl.get(atlas);
	}

	/**
	 * Finds the atlas sprite containing the vertex centroid of the quad.
	 * Vertex centroid is essentially the mean u,v coordinate - the intent being
	 * to find a point that is unambiguously inside the sprite (vs on an edge.)
	 *
	 * <p>Should be reliable for any convex quad or triangle. May fail for non-convex quads.
	 * Note that all the above refers to u,v coordinates. Geometric vertex does not matter,
	 * except to the extent it was used to determine u,v.
	 */
	Sprite find(QuadView quad);

	/**
	 * Alternative to {@link #find(QuadView)} when vertex centroid is already
	 * known or unsuitable.  Expects normalized (0-1) coordinates on the atlas texture,
	 * which should already be the case for u,v values in vanilla baked quads and in
	 * {@link QuadView} after calling {@link com.bookkeepersmc.notebook.api.renderer.v1.mesh.MutableQuadView#spriteBake(Sprite, int)}.
	 *
	 * <p>Coordinates must be in the sprite interior for reliable results. Generally will
	 * be easier to use {@link #find(QuadView)} unless you know the vertex
	 * centroid will somehow not be in the quad interior. This method will be slightly
	 * faster if you already have the centroid or another appropriate value.
	 */
	Sprite find(float u, float v);
}

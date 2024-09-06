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
package com.bookkeepersmc.notebook.impl.renderer;

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadView;
import com.bookkeepersmc.notebook.api.renderer.v1.model.SpriteFinder;

public class SpriteFinderImpl implements SpriteFinder {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpriteFinderImpl.class);

	private final Node root;
	private final SpriteAtlasTexture spriteAtlasTexture;
	private int badSpriteCount = 0;

	public SpriteFinderImpl(Map<Identifier, Sprite> sprites, SpriteAtlasTexture spriteAtlasTexture) {
		root = new Node(0.5f, 0.5f, 0.25f);
		this.spriteAtlasTexture = spriteAtlasTexture;
		sprites.values().forEach(root::add);
	}

	@Override
	public Sprite find(QuadView quad) {
		float u = 0;
		float v = 0;

		for (int i = 0; i < 4; i++) {
			u += quad.u(i);
			v += quad.v(i);
		}

		return find(u * 0.25f, v * 0.25f);
	}

	@Override
	public Sprite find(float u, float v) {
		return root.find(u, v);
	}

	private class Node {
		final float midU;
		final float midV;
		final float cellRadius;
		Object lowLow = null;
		Object lowHigh = null;
		Object highLow = null;
		Object highHigh = null;

		Node(float midU, float midV, float radius) {
			this.midU = midU;
			this.midV = midV;
			cellRadius = radius;
		}

		static final float EPS = 0.00001f;

		void add(Sprite sprite) {
			if (sprite.getMinU() < 0 - EPS || sprite.getMaxU() > 1 + EPS || sprite.getMinV() < 0 - EPS || sprite.getMaxV() > 1 + EPS) {
				// Sprite has broken bounds. This SHOULD NOT happen, but in the past some mods have broken this.
				// Prefer failing with a log warning rather than risking a stack overflow.
				if (badSpriteCount++ < 5) {
					String errorMessage = "SpriteFinderImpl: Skipping sprite {} with broken bounds [{}, {}]x[{}, {}]. Sprite bounds should be between 0 and 1.";
					LOGGER.error(errorMessage, sprite.getContents().getId(), sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
				}

				return;
			}

			final boolean lowU = sprite.getMinU() < midU - EPS;
			final boolean highU = sprite.getMaxU() > midU + EPS;
			final boolean lowV = sprite.getMinV() < midV - EPS;
			final boolean highV = sprite.getMaxV() > midV + EPS;

			if (lowU && lowV) {
				addInner(sprite, lowLow, -1, -1, q -> lowLow = q);
			}

			if (lowU && highV) {
				addInner(sprite, lowHigh, -1, 1, q -> lowHigh = q);
			}

			if (highU && lowV) {
				addInner(sprite, highLow, 1, -1, q -> highLow = q);
			}

			if (highU && highV) {
				addInner(sprite, highHigh, 1, 1, q -> highHigh = q);
			}
		}

		private void addInner(Sprite sprite, Object quadrant, int uStep, int vStep, Consumer<Object> setter) {
			if (quadrant == null) {
				setter.accept(sprite);
			} else if (quadrant instanceof Node) {
				((Node) quadrant).add(sprite);
			} else {
				Node n = new Node(midU + cellRadius * uStep, midV + cellRadius * vStep, cellRadius * 0.5f);

				if (quadrant instanceof Sprite) {
					n.add((Sprite) quadrant);
				}

				n.add(sprite);
				setter.accept(n);
			}
		}

		private Sprite find(float u, float v) {
			if (u < midU) {
				return v < midV ? findInner(lowLow, u, v) : findInner(lowHigh, u, v);
			} else {
				return v < midV ? findInner(highLow, u, v) : findInner(highHigh, u, v);
			}
		}

		private Sprite findInner(Object quadrant, float u, float v) {
			if (quadrant instanceof Sprite) {
				return (Sprite) quadrant;
			} else if (quadrant instanceof Node) {
				return ((Node) quadrant).find(u, v);
			} else {
				return spriteAtlasTexture.getSprite(MissingSprite.getMissingSpriteId());
			}
		}
	}

	public static SpriteFinderImpl get(SpriteAtlasTexture atlas) {
		return ((SpriteFinderAccess) atlas).fabric_spriteFinder();
	}

	public interface SpriteFinderAccess {
		SpriteFinderImpl fabric_spriteFinder();
	}
}

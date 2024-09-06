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

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;

import com.bookkeepersmc.notebook.api.renderer.v1.Renderer;
import com.bookkeepersmc.notebook.api.renderer.v1.RendererAccess;
import com.bookkeepersmc.notebook.api.renderer.v1.material.RenderMaterial;
import com.bookkeepersmc.notebook.api.renderer.v1.material.ShadeMode;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadEmitter;
import com.bookkeepersmc.notebook.api.renderer.v1.model.ModelHelper;
import com.bookkeepersmc.notebook.api.renderer.v1.render.RenderContext;
import com.bookkeepersmc.notebook.api.util.TriState;

public class VanillaModelEncoder {
	private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
	private static final RenderMaterial STANDARD_MATERIAL = RENDERER.materialFinder().shadeMode(ShadeMode.VANILLA).find();
	private static final RenderMaterial NO_AO_MATERIAL = RENDERER.materialFinder().shadeMode(ShadeMode.VANILLA).ambientOcclusion(TriState.FALSE).find();

	public static void emitBlockQuads(BakedModel model, @Nullable BlockState state, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		QuadEmitter emitter = context.getEmitter();
		final RenderMaterial defaultMaterial = model.useAmbientOcclusion() ? STANDARD_MATERIAL : NO_AO_MATERIAL;

		for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
			final Direction cullFace = ModelHelper.faceFromIndex(i);

			if (!context.hasTransform() && context.isFaceCulled(cullFace)) {
				// Skip entire quad list if possible.
				continue;
			}

			final List<BakedQuad> quads = model.getQuads(state, cullFace, randomSupplier.get());
			final int count = quads.size();

			for (int j = 0; j < count; j++) {
				final BakedQuad q = quads.get(j);
				emitter.fromVanilla(q, defaultMaterial, cullFace);
				emitter.emit();
			}
		}
	}

	public static void emitItemQuads(BakedModel model, @Nullable BlockState state, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		QuadEmitter emitter = context.getEmitter();

		for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
			final Direction cullFace = ModelHelper.faceFromIndex(i);
			final List<BakedQuad> quads = model.getQuads(state, cullFace, randomSupplier.get());
			final int count = quads.size();

			for (int j = 0; j < count; j++) {
				final BakedQuad q = quads.get(j);
				emitter.fromVanilla(q, STANDARD_MATERIAL, cullFace);
				emitter.emit();
			}
		}
	}
}

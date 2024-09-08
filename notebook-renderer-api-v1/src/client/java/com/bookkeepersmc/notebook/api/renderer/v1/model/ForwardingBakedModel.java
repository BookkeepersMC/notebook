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

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.unmapped.C_aaeuehbb;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import com.bookkeepersmc.notebook.api.renderer.v1.render.RenderContext;

/**
 * Base class for specialized model implementations that need to wrap other baked models.
 * Avoids boilerplate code for pass-through methods.
 */
public abstract class ForwardingBakedModel implements BakedModel, WrapperBakedModel {
	protected BakedModel wrapped;

	@Override
	public boolean isVanillaAdapter() {
		return wrapped.isVanillaAdapter();
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		wrapped.emitItemQuads(stack, randomSupplier, context);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction face, RandomGenerator rand) {
		return wrapped.getQuads(blockState, face, rand);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return wrapped.useAmbientOcclusion();
	}

	@Override
	public boolean hasDepth() {
		return wrapped.hasDepth();
	}

	@Override
	public boolean isBuiltin() {
		return wrapped.isBuiltin();
	}

	@Override
	public Sprite getParticleSprite() {
		return wrapped.getParticleSprite();
	}

	@Override
	public boolean isSideLit() {
		return wrapped.isSideLit();
	}

	@Override
	public ModelTransformation getTransformation() {
		return wrapped.getTransformation();
	}

	@Override
	public C_aaeuehbb method_4710() {
		return wrapped.method_4710();
	}

	@Override
	public BakedModel getWrappedModel() {
		return wrapped;
	}
}

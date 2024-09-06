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

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import com.bookkeepersmc.notebook.api.renderer.v1.render.RenderContext;
import com.bookkeepersmc.notebook.impl.renderer.VanillaModelEncoder;

/**
 * Interface for baked models that output meshes with enhanced rendering features.
 * Can also be used to generate or customize outputs based on world state instead of
 * or in addition to block state when render chunks are rebuilt.
 *
 * <p>Implementors should have a look at {@link ModelHelper} as it contains many useful functions.
 *
 * <p>Note: This interface is automatically implemented on all baked models via Mixin and interface injection.
 */
public interface NotebookBakedModel {
	/**
	 * When true, signals renderer this producer is implemented through {@link BakedModel#getQuads(BlockState, Direction, RandomGenerator)}.
	 * Also means the model does not rely on any non-vanilla features.
	 * Allows the renderer to optimize or route vanilla models through the unmodified vanilla pipeline if desired.
	 *
	 * <p>Vanilla baked models will return true.
	 * Enhanced models that use this API should return false,
	 * otherwise the API will not recognize the model.
	 */
	default boolean isVanillaAdapter() {
		return true;
	}

	/**
	 * This method will be called during chunk rebuilds to generate both the static and
	 * dynamic portions of a block model when the model implements this interface and
	 * {@link #isVanillaAdapter()} returns false.
	 *
	 * <p>During chunk rebuild, this method will always be called exactly one time per block
	 * position, irrespective of which or how many faces or block render layers are included
	 * in the model. Models must output all quads/meshes in a single pass.
	 *
	 * <p>Also called to render block models outside of chunk rebuild or block entity rendering.
	 * Typically, this happens when the block is being rendered as an entity, not as a block placed in the world.
	 * Currently, this happens for falling blocks and blocks being pushed by a piston, but renderers
	 * should invoke this for all calls to {@link net.minecraft.client.render.block.BlockModelRenderer#render(BlockRenderView, BakedModel, BlockState, BlockPos, MatrixStack, VertexConsumer, boolean, RandomGenerator, long, int)}
	 * that occur outside of chunk rebuilds to allow for features added by mods, unless
	 * {@link #isVanillaAdapter()} returns true.
	 *
	 * <p>Outside of chunk rebuilds, this method will be called every frame. Model implementations should
	 * rely on pre-baked meshes as much as possible and keep transformation to a minimum.  The provided
	 * block position may be the <em>nearest</em> block position and not actual. For this reason, neighbor
	 * state lookups are best avoided or will require special handling. Block entity lookups are
	 * likely to fail and/or give meaningless results.
	 *
	 * <p>In all cases, renderer will handle face occlusion and filter quads on faces obscured by
	 * neighboring blocks (if appropriate).  Models only need to consider "sides" to the
	 * extent the model is driven by connection with neighbor blocks or other world state.
	 *
	 * <p>Note: with {@link BakedModel#getQuads(BlockState, Direction, RandomGenerator)}, the random
	 * parameter is normally initialized with the same seed prior to each face layer.
	 * Model authors should note this method is called only once per block, and call the provided
	 * Random supplier multiple times if re-seeding is necessary.
	 *
	 * @param blockView Access to world state. Cast to {@code RenderAttachedBlockView} to
	 * retrieve block entity data unless thread safety can be guaranteed.
	 * @param state Block state for model being rendered.
	 * @param pos Position of block for model being rendered.
	 * @param randomSupplier  Random object seeded per vanilla conventions. Call multiple times to re-seed.
	 * Will not be thread-safe. Do not cache or retain a reference.
	 * @param context Accepts model output.
	 */
	default void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		VanillaModelEncoder.emitBlockQuads((BakedModel) this, state, randomSupplier, context);
	}

	/**
	 * This method will be called during item rendering to generate both the static and
	 * dynamic portions of an item model when the model implements this interface and
	 * {@link #isVanillaAdapter()} returns false.
	 *
	 * <p>Vanilla item rendering is normally very limited. It ignores lightmaps, vertex colors,
	 * and vertex normals. Renderers are expected to implement enhanced features for item
	 * models. If a feature is impractical due to performance or other concerns, then the
	 * renderer must at least give acceptable visual results without the need for special
	 * case handling in model implementations.
	 *
	 * <p>Calls to this method will generally happen on the main client thread but nothing
	 * prevents a mod or renderer from calling this method concurrently. Implementations
	 * should not mutate the ItemStack parameter, and best practice will be to make the
	 * method thread-safe.
	 *
	 * <p>Implementing this method does NOT mitigate the need to implement a functional
	 * {@link BakedModel#method_4710()} method, because this method will be called
	 * on the <em>result</em> of  {@link BakedModel#method_4710()}.  However, that
	 * method can simply return the base model because the output from this method will
	 * be used for rendering.
	 *
	 * <p>Renderer implementations should also use this method to obtain the quads used
	 * for item enchantment glint rendering.  This means models can put geometric variation
	 * logic here, instead of returning every possible shape from {@link BakedModel#method_4710()}
	 * as vanilla baked models.
	 */
	default void emitItemQuads(ItemStack stack, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		VanillaModelEncoder.emitItemQuads((BakedModel) this, null, randomSupplier, context);
	}
}

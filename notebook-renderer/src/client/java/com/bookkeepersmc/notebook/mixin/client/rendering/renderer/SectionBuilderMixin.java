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
package com.bookkeepersmc.notebook.mixin.client.rendering.renderer;

import java.util.Map;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkBufferStorage;
import net.minecraft.client.render.chunk.ChunkRenderRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import com.bookkeepersmc.notebook.impl.client.rendering.NotebookBuiltinRenderer;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.accessor.AccessChunkRendererRegion;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.render.TerrainRenderContext;

/**
 * Implements the main hooks for terrain rendering. Attempts to tread
 * lightly. This means we are deliberately stepping over some minor
 * optimization opportunities.
 *
 * <p>Non-Fabric renderer implementations that are looking to maximize
 * performance will likely take a much more aggressive approach.
 * For that reason, mod authors who want compatibility with advanced
 * renderers will do well to steer clear of chunk rebuild hooks unless
 * they are creating a renderer.
 *
 * <p>These hooks are intended only for the Fabric default renderer and
 * aren't expected to be present when a different renderer is being used.
 * Renderer authors are responsible for creating the hooks they need.
 * (Though they can use these as an example if they wish.)
 */
@Mixin(SectionBuilder.class)
public abstract class SectionBuilderMixin {
	@Shadow
	protected abstract BufferBuilder getOrCreateBuilder(Map<RenderLayer, BufferBuilder> builders, ChunkBufferStorage allocatorStorage, RenderLayer layer);

	@Inject(method = "build",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;iterate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void hookBuild(ChunkSectionPos sectionPos, ChunkRenderRegion region, VertexSorting sorter,
						ChunkBufferStorage allocators,
						CallbackInfoReturnable<SectionBuilder.Result> cir,
						@Local(ordinal = 0) Map<RenderLayer, BufferBuilder> builderMap) {
		// hook just before iterating over the render chunk's blocks to capture the buffer builder map
		TerrainRenderContext renderer = TerrainRenderContext.POOL.get();
		renderer.prepare(region, layer -> getOrCreateBuilder(builderMap, allocators, layer));
		((AccessChunkRendererRegion) region).notebook_setRenderer(renderer);
	}

	/**
	 * This is the hook that actually implements the rendering API for terrain rendering.
	 *
	 * <p>It's unusual to have a @Redirect in a Fabric library, but in this case
	 * it is our explicit intention that {@link BlockRenderManager#renderBlock(BlockState, BlockPos, BlockRenderView, MatrixStack, VertexConsumer, boolean, RandomGenerator)}
	 * does not execute for models that will be rendered by our renderer.
	 *
	 * <p>Any mod that wants to redirect this specific call is likely also a renderer, in which case this
	 * renderer should not be present, or the mod should probably instead be relying on the renderer API
	 * which was specifically created to provide for enhanced terrain rendering.
	 *
	 * <p>Note also that {@link BlockRenderManager#renderBlock(BlockState, BlockPos, BlockRenderView, MatrixStack, VertexConsumer, boolean, RandomGenerator)}
	 * IS called if the block render type is something other than {@link BlockRenderType#MODEL}.
	 * Normally this does nothing but will allow mods to create rendering hooks that are
	 * driven off of render type. (Not recommended or encouraged, but also not prevented.)
	 */
	@Redirect(method = "build", require = 1, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/random/RandomGenerator;)V"))
	private void hookBuildRenderBlock(BlockRenderManager renderManager, BlockState blockState, BlockPos blockPos, BlockRenderView blockView, MatrixStack matrix, VertexConsumer bufferBuilder, boolean checkSides, RandomGenerator random) {
		if (blockState.getRenderType() == BlockRenderType.MODEL) {
			final BakedModel model = renderManager.getModel(blockState);

			if (NotebookBuiltinRenderer.ALWAYS_TESSELATE_INDIGO || !model.isVanillaAdapter()) {
				((AccessChunkRendererRegion) blockView).notebook_getRenderer().tessellateBlock(blockState, blockPos, model, matrix);
				return;
			}
		}

		renderManager.renderBlock(blockState, blockPos, blockView, matrix, bufferBuilder, checkSides, random);
	}

	/**
	 * Release all references. Probably not necessary but would be $#%! to debug if it is.
	 */
	@Inject(method = "build", at = @At(value = "RETURN"))
	private void hookBuildReturn(ChunkSectionPos sectionPos, ChunkRenderRegion renderRegion, VertexSorting vertexSorter, ChunkBufferStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.Result> cir) {
		((AccessChunkRendererRegion) renderRegion).notebook_getRenderer().release();
		((AccessChunkRendererRegion) renderRegion).notebook_setRenderer(null);
	}
}

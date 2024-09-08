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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer.render;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import com.bookkeepersmc.notebook.api.renderer.v1.material.BlendMode;

/**
 * Holds, manages, and provides access to the block/world related state
 * needed to render quads.
 *
 * <p>Exception: per-block position offsets are tracked in {@link ChunkRenderInfo}
 * so they can be applied together with chunk offsets.
 */
public class BlockRenderInfo {
	private final BlockColors blockColorMap = Minecraft.getInstance().getBlockColors();
	private final BlockPos.Mutable searchPos = new BlockPos.Mutable();

	public BlockRenderView blockView;
	public BlockPos blockPos;
	public BlockState blockState;

	boolean useAo;
	boolean defaultAo;
	RenderLayer defaultLayer;

	RandomGenerator random;
	long seed;
	boolean recomputeSeed;
	public final Supplier<RandomGenerator> randomSupplier = () -> {
		long seed = this.seed;

		if (recomputeSeed) {
			seed = blockState.getRenderingSeed(blockPos);
			this.seed = seed;
			recomputeSeed = false;
		}

		final RandomGenerator random = this.random;
		random.setSeed(seed);
		return random;
	};

	private boolean enableCulling;
	private int cullCompletionFlags;
	private int cullResultFlags;

	public void prepareForWorld(BlockRenderView blockView, boolean enableCulling) {
		this.blockView = blockView;
		this.enableCulling = enableCulling;
	}

	public void prepareForBlock(BlockState blockState, BlockPos blockPos, boolean modelAo) {
		this.blockPos = blockPos;
		this.blockState = blockState;

		useAo = Minecraft.isAmbientOcclusionEnabled();
		defaultAo = useAo && modelAo && blockState.getLuminance() == 0;

		defaultLayer = RenderLayers.getBlockLayer(blockState);

		cullCompletionFlags = 0;
		cullResultFlags = 0;
	}

	public void release() {
		blockView = null;
		blockPos = null;
		blockState = null;
	}

	int blockColor(int colorIndex) {
		return 0xFF000000 | blockColorMap.getColor(blockState, blockView, blockPos, colorIndex);
	}

	boolean shouldDrawFace(@Nullable Direction face) {
		if (face == null || !enableCulling) {
			return true;
		}

		final int mask = 1 << face.getId();

		if ((cullCompletionFlags & mask) == 0) {
			cullCompletionFlags |= mask;

			if (Block.shouldDrawSide(blockState, blockView.getBlockState(searchPos.set(blockPos, face)), face)) {
				cullResultFlags |= mask;
				return true;
			} else {
				return false;
			}
		} else {
			return (cullResultFlags & mask) != 0;
		}
	}

	RenderLayer effectiveRenderLayer(BlendMode blendMode) {
		return blendMode == BlendMode.DEFAULT ? this.defaultLayer : blendMode.blockRenderLayer;
	}
}

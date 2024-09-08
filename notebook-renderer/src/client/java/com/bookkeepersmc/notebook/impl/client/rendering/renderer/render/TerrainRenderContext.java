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

import java.util.function.Function;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkRenderRegion;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;

import com.bookkeepersmc.notebook.api.renderer.v1.render.RenderContext;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc.AoCalculator;

/**
 * Implementation of {@link RenderContext} used during terrain rendering.
 * Dispatches calls from models during chunk rebuild to the appropriate consumer,
 * and holds/manages all of the state needed by them.
 */
public class TerrainRenderContext extends AbstractBlockRenderContext {
	public static final ThreadLocal<TerrainRenderContext> POOL = ThreadLocal.withInitial(TerrainRenderContext::new);

	private final ChunkRenderInfo chunkInfo = new ChunkRenderInfo();

	public TerrainRenderContext() {
		overlay = OverlayTexture.DEFAULT_UV;
		blockInfo.random = RandomGenerator.createLegacy();
	}

	@Override
	protected AoCalculator createAoCalc(BlockRenderInfo blockInfo) {
		return new AoCalculator(blockInfo) {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return chunkInfo.cachedBrightness(pos, state);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return chunkInfo.cachedAoLevel(pos, state);
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(RenderLayer layer) {
		return chunkInfo.getBuffer(layer);
	}

	public void prepare(ChunkRenderRegion blockView, Function<RenderLayer, BufferBuilder> bufferFunc) {
		chunkInfo.prepare(blockView, bufferFunc);
		blockInfo.prepareForWorld(blockView, true);
	}

	public void release() {
		chunkInfo.release();
		blockInfo.release();
	}

	/** Called from chunk renderer hook. */
	public void tessellateBlock(BlockState blockState, BlockPos blockPos, final BakedModel model, MatrixStack matrixStack) {
		try {
			Vec3d offset = blockState.getModelOffset(blockPos);
			matrixStack.translate(offset.x, offset.y, offset.z);

			this.matrix = matrixStack.peek().getModel();
			this.normalMatrix = matrixStack.peek().getNormal();

			blockInfo.recomputeSeed = true;

			aoCalc.clear();
			blockInfo.prepareForBlock(blockState, blockPos, model.useAmbientOcclusion());
			model.emitBlockQuads(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, blockInfo.randomSupplier, this);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.create(throwable, "Tessellating block in world - Indigo Renderer");
			CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
			CrashReportSection.addBlockInfo(crashReportSection, chunkInfo.blockView, blockPos, blockState);
			throw new CrashException(crashReport);
		}
	}
}

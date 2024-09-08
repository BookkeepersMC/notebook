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

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc.AoCalculator;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc.AoLuminanceFix;

/**
 * Context for non-terrain block rendering.
 */
public class BlockRenderContext extends AbstractBlockRenderContext {
	private VertexConsumer vertexConsumer;

	@Override
	protected AoCalculator createAoCalc(BlockRenderInfo blockInfo) {
		return new AoCalculator(blockInfo) {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return AoCalculator.getLightmapCoordinates(blockInfo.blockView, state, pos);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return AoLuminanceFix.INSTANCE.apply(blockInfo.blockView, pos, state);
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(RenderLayer layer) {
		return vertexConsumer;
	}

	public void render(BlockRenderView blockView, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrixStack, VertexConsumer buffer, boolean cull, RandomGenerator random, long seed, int overlay) {
		try {
			Vec3d offset = state.getModelOffset(pos);
			matrixStack.translate(offset.x, offset.y, offset.z);

			this.vertexConsumer = buffer;
			this.matrix = matrixStack.peek().getModel();
			this.normalMatrix = matrixStack.peek().getNormal();
			this.overlay = overlay;

			blockInfo.random = random;
			blockInfo.seed = seed;
			blockInfo.recomputeSeed = false;

			aoCalc.clear();
			blockInfo.prepareForWorld(blockView, cull);
			blockInfo.prepareForBlock(state, pos, model.useAmbientOcclusion());

			model.emitBlockQuads(blockView, state, pos, blockInfo.randomSupplier, this);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.create(throwable, "Tessellating block model - Indigo Renderer");
			CrashReportSection crashReportSection = crashReport.addElement("Block model being tessellated");
			CrashReportSection.addBlockInfo(crashReportSection, blockView, pos, state);
			throw new CrashException(crashReport);
		} finally {
			blockInfo.release();
			blockInfo.random = null;
			this.vertexConsumer = null;
		}
	}
}

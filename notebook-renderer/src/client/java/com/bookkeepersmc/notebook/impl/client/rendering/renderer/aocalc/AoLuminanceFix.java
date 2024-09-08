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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import com.bookkeepersmc.notebook.impl.client.rendering.NotebookBuiltinRenderer;

/**
 * Implements a fix to prevent luminous blocks from casting AO shade.
 * Will give normal result if fix is disabled.
 */
@FunctionalInterface
public interface AoLuminanceFix {
	float apply(BlockView view, BlockPos pos, BlockState state);

	AoLuminanceFix INSTANCE = NotebookBuiltinRenderer.FIX_LUMINOUS_AO_SHADE ? AoLuminanceFix::fixed : AoLuminanceFix::vanilla;

	static float vanilla(BlockView view, BlockPos pos, BlockState state) {
		return state.getAmbientOcclusionLightLevel(view, pos);
	}

	static float fixed(BlockView view, BlockPos pos, BlockState state) {
		return state.getLuminance() == 0 ? state.getAmbientOcclusionLightLevel(view, pos) : 1f;
	}
}

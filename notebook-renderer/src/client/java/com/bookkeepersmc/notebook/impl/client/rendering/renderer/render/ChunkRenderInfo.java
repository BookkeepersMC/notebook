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
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkRenderRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc.AoCalculator;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc.AoLuminanceFix;

/**
 * Holds, manages and provides access to the chunk-related state
 * needed by fallback and mesh consumers during terrain rendering.
 *
 * <p>Exception: per-block position offsets are tracked here so they can
 * be applied together with chunk offsets.
 */
public class ChunkRenderInfo {
	/**
	 * Serves same function as brightness cache in Mojang's AO calculator,
	 * with some differences as follows...
	 *
	 * <ul><li>Mojang uses Object2Int.  This uses Long2Int for performance and to avoid
	 * creating new immutable BlockPos references.  But will break if someone
	 * wants to expand Y limit or world borders.  If we want to support that may
	 * need to switch or make configurable.
	 *
	 * <li>Mojang overrides the map methods to limit the cache to 50 values.
	 * However, a render chunk only has 18^3 blocks in it, and the cache is cleared every chunk.
	 * For performance and simplicity, we just let map grow to the size of the render chunk.
	 *
	 * <li>Mojang only uses the cache for Ao.  Here it is used for all brightness
	 * lookups, including flat lighting.
	 *
	 * <li>The Mojang cache is a separate threadlocal with a threadlocal boolean to
	 * enable disable. Cache clearing happens with the disable. There's no use case for
	 * us when the cache needs to be disabled (and no apparent case in Mojang's code either)
	 * so we simply clear the cache at the start of each new chunk. It is also
	 * not a threadlocal because it's held within a threadlocal BlockRenderer.</ul>
	 */
	private final Long2IntOpenHashMap brightnessCache;
	private final Long2FloatOpenHashMap aoLevelCache;

	private Function<RenderLayer, BufferBuilder> bufferFunc;
	BlockRenderView blockView;

	ChunkRenderInfo() {
		brightnessCache = new Long2IntOpenHashMap();
		brightnessCache.defaultReturnValue(Integer.MAX_VALUE);
		aoLevelCache = new Long2FloatOpenHashMap();
		aoLevelCache.defaultReturnValue(Float.MAX_VALUE);
	}

	void prepare(ChunkRenderRegion blockView, Function<RenderLayer, BufferBuilder> bufferFunc) {
		this.blockView = blockView;
		this.bufferFunc = bufferFunc;

		brightnessCache.clear();
		aoLevelCache.clear();
	}

	void release() {
		blockView = null;
		bufferFunc = null;
	}

	BufferBuilder getBuffer(RenderLayer layer) {
		return bufferFunc.apply(layer);
	}

	/**
	 * Cached values for {@link WorldRenderer#getLightmapCoordinates(BlockRenderView, BlockState, BlockPos)}.
	 * See also the comments for {@link #brightnessCache}.
	 */
	int cachedBrightness(BlockPos pos, BlockState state) {
		long key = pos.asLong();
		int result = brightnessCache.get(key);

		if (result == Integer.MAX_VALUE) {
			result = AoCalculator.getLightmapCoordinates(blockView, state, pos);
			brightnessCache.put(key, result);
		}

		return result;
	}

	float cachedAoLevel(BlockPos pos, BlockState state) {
		long key = pos.asLong();
		float result = aoLevelCache.get(key);

		if (result == Float.MAX_VALUE) {
			result = AoLuminanceFix.INSTANCE.apply(blockView, pos, state);
			aoLevelCache.put(key, result);
		}

		return result;
	}
}

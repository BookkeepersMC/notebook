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
package com.bookkeepersmc.notebook.impl.event.lifecycle;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.WorldChunk;

import com.bookkeepersmc.api.ModInitializer;
import com.bookkeepersmc.notebook.api.event.lifecycle.v1.ServerBlockEntityEvents;
import com.bookkeepersmc.notebook.api.event.lifecycle.v1.ServerChunkEvents;
import com.bookkeepersmc.notebook.api.event.lifecycle.v1.ServerEntityEvents;
import com.bookkeepersmc.notebook.api.event.lifecycle.v1.ServerWorldEvents;

public final class LifecycleEventsImpl implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			((LoadedChunksCache) world).notebook_markLoaded(chunk);
		});

		ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
			((LoadedChunksCache) world).notebook_markUnloaded(chunk);
		});

		ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
			for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
				ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, world);
			}
		});

		ServerWorldEvents.UNLOAD.register((server, world) -> {
			for (WorldChunk chunk : ((LoadedChunksCache) world).notebook_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, world);
				}
			}

			for (Entity entity : world.iterateEntities()) {
				ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, world);
			}
		});
	}
}

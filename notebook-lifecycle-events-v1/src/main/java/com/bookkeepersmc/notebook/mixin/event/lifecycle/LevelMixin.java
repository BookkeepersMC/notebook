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
package com.bookkeepersmc.notebook.mixin.event.lifecycle;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import com.bookkeepersmc.notebook.impl.event.lifecycle.LoadedChunksCache;

@Mixin(World.class)
public abstract class LevelMixin implements LoadedChunksCache {
	@Unique
	private final Set<WorldChunk> loadedChunks = new HashSet<>();

	@Override
	public Set<WorldChunk> notebook_getLoadedChunks() {
		return this.loadedChunks;
	}

	@Override
	public void notebook_markLoaded(WorldChunk chunk) {
		this.loadedChunks.add(chunk);
	}

	@Override
	public void notebook_markUnloaded(WorldChunk chunk) {
		this.loadedChunks.remove(chunk);
	}
}

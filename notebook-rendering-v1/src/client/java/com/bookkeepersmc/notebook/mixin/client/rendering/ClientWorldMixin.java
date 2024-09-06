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
package com.bookkeepersmc.notebook.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.color.biome.BiomeColorProvider;
import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import com.bookkeepersmc.notebook.impl.client.rendering.ColorResolverRegistryImpl;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	@Shadow
	public abstract int calculateColor(BlockPos pos, BiomeColorProvider provider);

	@Unique
	private final Reference2ReferenceMap<BiomeColorProvider, BiomeColorCache> customColorCache = ColorResolverRegistryImpl.createCustomCacheMap(resolver -> new BiomeColorCache(pos -> calculateColor(pos, resolver)));

	// I really can't inject an "onChunkLoaded" into the onChunkLoaded method :/
	@Inject(method = "onChunkLoaded", at = @At("RETURN"))
	private void onWhenChunkLoads(ChunkPos pos, CallbackInfo info) {
		for (BiomeColorCache cache : customColorCache.values()) {
			cache.reset();
		}
	}

	@ModifyExpressionValue(method = "getColor", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2ObjectArrayMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object modifyNullCache(Object cache, BlockPos pos, BiomeColorProvider provider) {
		if (cache == null) {
			cache = customColorCache.get(provider);

			if (cache == null) {
				throw new UnsupportedOperationException("ClientWorld.getColor() called with unregistered BiomeColorProvider " + provider);
			}
		}

		return cache;
	}
}

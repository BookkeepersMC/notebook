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
package com.bookkeepersmc.notebook.mixin.event.lifecycle.client;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.data.ChunkData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import com.bookkeepersmc.notebook.api.client.event.lifecycle.v1.ClientChunkEvents;

@Mixin(ClientChunkManager.class)
public abstract class ClientChunkCacheMixin {
	@Final
	@Shadow
	private ClientWorld world;

	@Inject(method = "loadChunkFromPacket", at = @At("TAIL"))
	private void onChunkLoad(int x, int z, PacketByteBuf packetByteBuf, NbtCompound nbtCompound, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> info) {
		ClientChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.world, info.getReturnValue());
	}

	@Inject(method = "loadChunkFromPacket", at = @At(value = "NEW", target = "net/minecraft/world/chunk/WorldChunk", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
	private void onChunkUnload(int x, int z, PacketByteBuf buf, NbtCompound tag, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> info, int index, WorldChunk worldChunk, ChunkPos chunkPos) {
		if (worldChunk != null) {
			ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, worldChunk);
		}
	}

	@Inject(method = "unload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager$ClientChunkMap;method_62893(ILnet/minecraft/world/chunk/WorldChunk;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void onChunkUnload(ChunkPos pos, CallbackInfo ci, int i, WorldChunk chunk) {
		ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, chunk);
	}

	@Inject(
			method = "updateLoadDistance",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/world/ClientChunkManager$ClientChunkMap;isInRadius(II)Z"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onUpdateLoadDistance(int loadDistance, CallbackInfo ci, int oldRadius, int newRadius, ClientChunkManager.ClientChunkMap clientChunkMap, int k, WorldChunk oldChunk, ChunkPos chunkPos) {
		if (!clientChunkMap.isInRadius(chunkPos.x, chunkPos.z)) {
			ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, oldChunk);
		}
	}
}

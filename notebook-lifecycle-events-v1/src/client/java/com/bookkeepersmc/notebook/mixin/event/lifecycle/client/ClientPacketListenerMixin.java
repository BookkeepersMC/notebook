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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import com.bookkeepersmc.notebook.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import com.bookkeepersmc.notebook.api.client.event.lifecycle.v1.ClientEntityEvents;
import com.bookkeepersmc.notebook.impl.event.lifecycle.LoadedChunksCache;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
	@Shadow
	private ClientLevel level;

	@Inject(method = "handleRespawn", at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientLevel"))
	private void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.level != null) {
			for (Entity entity : this.level.entitiesForRendering()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
			}

			for (LevelChunk chunk : ((LoadedChunksCache) this.level).notebook_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
				}
			}
		}
	}

	/**
	 * An explanation why we unload entities during onGameJoin:
	 * Proxies such as Waterfall may send another Game Join packet if entity meta rewrite is disabled, so we will cover ourselves.
	 * Velocity by default will send a Game Join packet when the player changes servers, which will create a new client world.
	 * Also anyone can send another GameJoinPacket at any time, so we need to watch out.
	 */
	@Inject(method = "handleLogin", at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientLevel"))
	private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.level != null) {
			for (Entity entity : level.entitiesForRendering()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
			}

			for (LevelChunk chunk : ((LoadedChunksCache) this.level).notebook_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
				}
			}
		}
	}

	// Called when the client disconnects from a server or enters reconfiguration.
	@Inject(method = "clearLevel", at = @At("HEAD"))
	private void onClearWorld(CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.level != null) {
			for (Entity entity : this.level.entitiesForRendering()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
			}

			for (LevelChunk chunk : ((LoadedChunksCache) this.level).notebook_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
				}
			}
		}
	}
}

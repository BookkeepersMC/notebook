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
package com.bookkeepersmc.notebook.mixin.networking;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.phase.NetworkPhaseProtocol;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.networking.ChannelInfoHolder;
import com.bookkeepersmc.notebook.impl.networking.NetworkHandlerExtensions;
import com.bookkeepersmc.notebook.impl.networking.PacketCallbackListener;

@Mixin(ClientConnection.class)
abstract class ClientConnectionMixin implements ChannelInfoHolder {
	@Shadow
	private PacketListener packetListener;

	@Unique
	private Map<NetworkPhase, Collection<Identifier>> playChannels;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddedFields(NetworkSide side, CallbackInfo ci) {
		this.playChannels = new ConcurrentHashMap<>();
	}

	@Inject(method = "sendImmediately", at = @At(value = "FIELD", target = "Lnet/minecraft/network/ClientConnection;packetsSentCounter:I"))
	private void checkPacket(Packet<?> packet, PacketSendListener callback, boolean flush, CallbackInfo ci) {
		if (this.packetListener instanceof PacketCallbackListener) {
			((PacketCallbackListener) this.packetListener).sent(packet);
		}
	}

	@Inject(method = "validateInboundProtocol", at = @At("HEAD"))
	private void unwatchAddon(NetworkPhaseProtocol<?> state, PacketListener listener, CallbackInfo ci) {
		if (this.packetListener instanceof NetworkHandlerExtensions oldListener) {
			oldListener.getAddon().endSession();
		}
	}

	@Inject(method = "channelInactive", at = @At("HEAD"))
	private void disconnectAddon(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) {
		if (packetListener instanceof NetworkHandlerExtensions extension) {
			extension.getAddon().handleDisconnect();
		}
	}

	@Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/listener/PacketListener;onDisconnected(Lnet/minecraft/network/DisconnectionDetails;)V"))
	private void disconnectAddon(CallbackInfo ci) {
		if (packetListener instanceof NetworkHandlerExtensions extension) {
			extension.getAddon().handleDisconnect();
		}
	}

	@Override
	public Collection<Identifier> notebook_getPendingChannelsNames(NetworkPhase state) {
		return this.playChannels.computeIfAbsent(state, (key) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
	}
}

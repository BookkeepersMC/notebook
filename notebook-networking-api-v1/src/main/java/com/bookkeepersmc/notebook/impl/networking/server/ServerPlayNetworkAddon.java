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
package com.bookkeepersmc.notebook.impl.networking.server;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.networking.v1.PacketSender;
import com.bookkeepersmc.notebook.api.networking.v1.S2CPlayChannelEvents;
import com.bookkeepersmc.notebook.api.networking.v1.ServerPlayConnectionEvents;
import com.bookkeepersmc.notebook.api.networking.v1.ServerPlayNetworking;
import com.bookkeepersmc.notebook.impl.networking.AbstractChanneledNetworkAddon;
import com.bookkeepersmc.notebook.impl.networking.ChannelInfoHolder;
import com.bookkeepersmc.notebook.impl.networking.NetworkingImpl;
import com.bookkeepersmc.notebook.impl.networking.RegistrationPayload;

public final class ServerPlayNetworkAddon extends AbstractChanneledNetworkAddon<ServerPlayNetworking.PlayPayloadHandler<?>> {
	private final ServerPlayNetworkHandler handler;
	private final MinecraftServer server;
	private boolean sentInitialRegisterPacket;
	private final ServerPlayNetworking.Context context;

	public ServerPlayNetworkAddon(ServerPlayNetworkHandler handler, ClientConnection connection, MinecraftServer server) {
		super(ServerNetworkingImpl.PLAY, connection, "ServerPlayNetworkAddon for " + handler.player.getDisplayName());
		this.handler = handler;
		this.server = server;
		this.context = new ContextImpl(server, handler, this);

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, NetworkPhase.PLAY);
	}

	@Override
	protected void invokeInitEvent() {
		ServerPlayConnectionEvents.INIT.invoker().onPlayInit(this.handler, this.server);
	}

	public void onClientReady() {
		ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(this.handler, this, this.server);

		this.sendInitialChannelRegistrationPacket();
		this.sentInitialRegisterPacket = true;
	}

	@Override
	protected void receive(ServerPlayNetworking.PlayPayloadHandler<?> payloadHandler, CustomPayload payload) {
		this.server.execute(() -> {
			((ServerPlayNetworking.PlayPayloadHandler) payloadHandler).receive(payload, ServerPlayNetworkAddon.this.context);
		});
	}

	// impl details

	@Override
	protected void schedule(Runnable task) {
		this.handler.player.server.execute(task);
	}

	@Override
	public Packet<?> createPacket(CustomPayload packet) {
		return ServerPlayNetworking.createS2CPacket(packet);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		S2CPlayChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.server, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		S2CPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.server, ids);
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		// If we can already send packets, immediately send the register packet for this channel
		if (this.sentInitialRegisterPacket) {
			RegistrationPayload registrationPayload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName));

			if (registrationPayload != null) {
				this.sendPacket(registrationPayload);
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.sentInitialRegisterPacket) {
			RegistrationPayload registrationPayload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName));

			if (registrationPayload != null) {
				this.sendPacket(registrationPayload);
			}
		}
	}

	@Override
	protected void invokeDisconnectEvent() {
		ServerPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(this.handler, this.server);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}

	private record ContextImpl(MinecraftServer server, ServerPlayNetworkHandler handler, PacketSender responseSender) implements ServerPlayNetworking.Context {
		private ContextImpl {
			Objects.requireNonNull(server, "server");
			Objects.requireNonNull(handler, "handler");
			Objects.requireNonNull(responseSender, "responseSender");
		}

		@Override
		public ServerPlayerEntity player() {
			return handler.getPlayer();
		}
	}
}

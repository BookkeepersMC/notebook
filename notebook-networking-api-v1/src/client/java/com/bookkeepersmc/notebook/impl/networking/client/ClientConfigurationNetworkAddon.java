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
package com.bookkeepersmc.notebook.impl.networking.client;

import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.payload.BrandPayload;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.client.networking.v1.C2SConfigurationChannelEvents;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientConfigurationConnectionEvents;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientConfigurationNetworking;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientPlayNetworking;
import com.bookkeepersmc.notebook.api.networking.v1.PacketSender;
import com.bookkeepersmc.notebook.impl.networking.ChannelInfoHolder;
import com.bookkeepersmc.notebook.impl.networking.RegistrationPayload;
import com.bookkeepersmc.notebook.mixin.networking.client.accessor.ClientCommonNetworkHandlerAccessor;
import com.bookkeepersmc.notebook.mixin.networking.client.accessor.ClientConfigurationNetworkHandlerAccessor;

public final class ClientConfigurationNetworkAddon extends ClientCommonNetworkAddon<ClientConfigurationNetworking.ConfigurationPayloadHandler<?>, ClientConfigurationNetworkHandler> {
	private final ContextImpl context;
	private boolean sentInitialRegisterPacket;
	private boolean hasStarted;

	public ClientConfigurationNetworkAddon(ClientConfigurationNetworkHandler handler, Minecraft client) {
		super(ClientNetworkingImpl.CONFIGURATION, ((ClientCommonNetworkHandlerAccessor) handler).getConnection(), "ClientPlayNetworkAddon for " + ((ClientConfigurationNetworkHandlerAccessor) handler).getProfile().getName(), handler, client);
		this.context = new ContextImpl(client, this);

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, NetworkPhase.CONFIGURATION);
	}

	@Override
	protected void invokeInitEvent() {
		ClientConfigurationConnectionEvents.INIT.invoker().onConfigurationInit(this.handler, this.client);
	}

	@Override
	public void onServerReady() {
		super.onServerReady();
		invokeStartEvent();
	}

	@Override
	protected void receiveRegistration(boolean register, RegistrationPayload payload) {
		super.receiveRegistration(register, payload);

		if (register && !this.sentInitialRegisterPacket) {
			this.sendInitialChannelRegistrationPacket();
			this.sentInitialRegisterPacket = true;

			this.onServerReady();
		}
	}

	@Override
	public boolean handle(CustomPayload payload) {
		boolean result = super.handle(payload);

		if (payload instanceof BrandPayload) {
			// If we have received this without first receiving the registration packet, its likely a vanilla server.
			invokeStartEvent();
		}

		return result;
	}

	private void invokeStartEvent() {
		if (!hasStarted) {
			hasStarted = true;
			ClientConfigurationConnectionEvents.START.invoker().onConfigurationStart(this.handler, this.client);
		}
	}

	@Override
	protected void receive(ClientConfigurationNetworking.ConfigurationPayloadHandler<?> handler, CustomPayload payload) {
		((ClientConfigurationNetworking.ConfigurationPayloadHandler) handler).receive(payload, this.context);
	}

	// impl details
	@Override
	public Packet<?> createPacket(CustomPayload packet) {
		return ClientPlayNetworking.createC2SPacket(packet);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		C2SConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.client, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		C2SConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.client, ids);
	}

	public void handleComplete() {
		ClientConfigurationConnectionEvents.COMPLETE.invoker().onConfigurationComplete(this.handler, this.client);
		ClientConfigurationConnectionEvents.READY.invoker().onConfigurationReady(this.handler, this.client);
		ClientNetworkingImpl.setClientConfigurationAddon(null);
	}

	@Override
	protected void invokeDisconnectEvent() {
		ClientConfigurationConnectionEvents.DISCONNECT.invoker().onConfigurationDisconnect(this.handler, this.client);
	}

	public ChannelInfoHolder getChannelInfoHolder() {
		return (ChannelInfoHolder) ((ClientCommonNetworkHandlerAccessor) handler).getConnection();
	}

	private record ContextImpl(Minecraft client, PacketSender responseSender) implements ClientConfigurationNetworking.Context {
		private ContextImpl {
			Objects.requireNonNull(client, "client");
			Objects.requireNonNull(responseSender, "responseSender");
		}
	}
}

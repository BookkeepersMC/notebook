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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.client.networking.v1.ClientLoginConnectionEvents;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientLoginNetworking;
import com.bookkeepersmc.notebook.api.networking.v1.ByteBufUtils;
import com.bookkeepersmc.notebook.impl.networking.AbstractNetworkAddon;
import com.bookkeepersmc.notebook.impl.networking.payload.PacketByteBufLoginQueryRequestPayload;
import com.bookkeepersmc.notebook.impl.networking.payload.PacketByteBufLoginQueryResponse;
import com.bookkeepersmc.notebook.mixin.networking.client.accessor.ClientLoginNetworkHandlerAccessor;

public final class ClientLoginNetworkAddon extends AbstractNetworkAddon<ClientLoginNetworking.LoginQueryRequestHandler> {
	private final ClientLoginNetworkHandler handler;
	private final Minecraft client;
	private boolean firstResponse = true;

	public ClientLoginNetworkAddon(ClientLoginNetworkHandler handler, Minecraft client) {
		super(ClientNetworkingImpl.LOGIN, "ClientLoginNetworkAddon for Client");
		this.handler = handler;
		this.client = client;
	}

	@Override
	protected void invokeInitEvent() {
		ClientLoginConnectionEvents.INIT.invoker().onLoginStart(this.handler, this.client);
	}

	public boolean handlePacket(LoginQueryRequestS2CPacket packet) {
		PacketByteBufLoginQueryRequestPayload payload = (PacketByteBufLoginQueryRequestPayload) packet.payload();
		return handlePacket(packet.queryId(), packet.payload().id(), payload.data());
	}

	private boolean handlePacket(int queryId, Identifier channelName, PacketByteBuf originalBuf) {
		this.logger.debug("Handling inbound login response with id {} and channel with name {}", queryId, channelName);

		if (this.firstResponse) {
			ClientLoginConnectionEvents.QUERY_START.invoker().onLoginQueryStart(this.handler, this.client);
			this.firstResponse = false;
		}

		@Nullable ClientLoginNetworking.LoginQueryRequestHandler handler = this.getHandler(channelName);

		if (handler == null) {
			return false;
		}

		PacketByteBuf buf = ByteBufUtils.slice(originalBuf);
		List<PacketSendListener> callbacks = new ArrayList<>();

		try {
			CompletableFuture<@Nullable PacketByteBuf> future = handler.receive(this.client, this.handler, buf, callbacks::add);
			future.thenAccept(result -> {
				LoginQueryResponseC2SPacket packet = new LoginQueryResponseC2SPacket(queryId, result == null ? null : new PacketByteBufLoginQueryResponse(result));
				((ClientLoginNetworkHandlerAccessor) this.handler).getConnection().send(packet, new PacketSendListener() {
					@Override
					public void onSuccess() {
						callbacks.forEach(PacketSendListener::onSuccess);
					}
				});
			});
		} catch (Throwable ex) {
			this.logger.error("Encountered exception while handling in channel with name \"{}\"", channelName, ex);
			throw ex;
		}

		return true;
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
	}

	@Override
	protected void invokeDisconnectEvent() {
		ClientLoginConnectionEvents.DISCONNECT.invoker().onLoginDisconnect(this.handler, this.client);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return false;
	}
}

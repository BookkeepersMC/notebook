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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import com.bookkeepersmc.notebook.api.networking.v1.ByteBufUtils;
import com.bookkeepersmc.notebook.api.networking.v1.LoginPacketSender;
import com.bookkeepersmc.notebook.api.networking.v1.ServerLoginConnectionEvents;
import com.bookkeepersmc.notebook.api.networking.v1.ServerLoginNetworking;
import com.bookkeepersmc.notebook.impl.networking.AbstractNetworkAddon;
import com.bookkeepersmc.notebook.impl.networking.payload.FriendlyByteBufLoginQueryRequestPayload;
import com.bookkeepersmc.notebook.impl.networking.payload.FriendlyByteBufLoginQueryResponse;
import com.bookkeepersmc.notebook.mixin.networking.accessor.ServerLoginNetworkHandlerAccessor;

public final class ServerLoginNetworkAddon extends AbstractNetworkAddon<ServerLoginNetworking.LoginQueryResponseHandler> implements LoginPacketSender {
	private final Connection connection;
	private final ServerLoginPacketListenerImpl handler;
	private final MinecraftServer server;
	private final QueryIdFactory queryIdFactory;
	private final Collection<Future<?>> waits = new ConcurrentLinkedQueue<>();
	private final Map<Integer, ResourceLocation> channels = new ConcurrentHashMap<>();
	private boolean firstQueryTick = true;

	public ServerLoginNetworkAddon(ServerLoginPacketListenerImpl handler) {
		super(ServerNetworkingImpl.LOGIN, "ServerLoginNetworkAddon for " + handler.getUserName());
		this.connection = ((ServerLoginNetworkHandlerAccessor) handler).getConnection();
		this.handler = handler;
		this.server = ((ServerLoginNetworkHandlerAccessor) handler).getServer();
		this.queryIdFactory = QueryIdFactory.create();
	}

	@Override
	protected void invokeInitEvent() {
		ServerLoginConnectionEvents.INIT.invoker().onLoginInit(handler, this.server);
	}

	// return true if no longer ticks query
	public boolean queryTick() {
		if (this.firstQueryTick) {
			// Send the compression packet now so clients receive compressed login queries
			this.sendCompressionPacket();

			ServerLoginConnectionEvents.QUERY_START.invoker().onLoginStart(this.handler, this.server, this, this.waits::add);
			this.firstQueryTick = false;
		}

		AtomicReference<Throwable> error = new AtomicReference<>();
		this.waits.removeIf(future -> {
			if (!future.isDone()) {
				return false;
			}

			try {
				future.get();
			} catch (ExecutionException ex) {
				Throwable caught = ex.getCause();
				error.getAndUpdate(oldEx -> {
					if (oldEx == null) {
						return caught;
					}

					oldEx.addSuppressed(caught);
					return oldEx;
				});
			} catch (InterruptedException | CancellationException ignored) {
				// ignore
			}

			return true;
		});

		return this.channels.isEmpty() && this.waits.isEmpty();
	}

	private void sendCompressionPacket() {
		// Compression is not needed for local transport
		if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
			this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
					PacketSendListener.thenRun(() -> connection.setupCompression(server.getCompressionThreshold(), true))
			);
		}
	}

	/**
	 * Handles an incoming query response during login.
	 *
	 * @param packet the packet to handle
	 * @return true if the packet was handled
	 */
	public boolean handle(ServerboundCustomQueryAnswerPacket packet) {
		FriendlyByteBufLoginQueryResponse response = (FriendlyByteBufLoginQueryResponse) packet.payload();
		return handle(packet.transactionId(), response == null ? null : response.data());
	}

	private boolean handle(int queryId, @Nullable FriendlyByteBuf originalBuf) {
		this.logger.debug("Handling inbound login query with id {}", queryId);
		ResourceLocation channel = this.channels.remove(queryId);

		if (channel == null) {
			this.logger.warn("Query ID {} was received but no query has been associated in {}!", queryId, this.connection);
			return false;
		}

		boolean understood = originalBuf != null;
		@Nullable ServerLoginNetworking.LoginQueryResponseHandler handler = this.getHandler(channel);

		if (handler == null) {
			return false;
		}

		FriendlyByteBuf buf = understood ? ByteBufUtils.slice(originalBuf) : ByteBufUtils.empty();

		try {
			handler.receive(this.server, this.handler, understood, buf, this.waits::add, this);
		} catch (Throwable ex) {
			this.logger.error("Encountered exception while handling in channel \"{}\"", channel, ex);
			throw ex;
		}

		return true;
	}

	@Override
	public Packet<?> createPacket(CustomPacketPayload packet) {
		throw new UnsupportedOperationException("Cannot send CustomPayload during login");
	}

	@Override
	public Packet<?> createPacket(ResourceLocation channelName, FriendlyByteBuf buf) {
		int queryId = this.queryIdFactory.nextId();
		return new ClientboundCustomQueryPacket(queryId, new FriendlyByteBufLoginQueryRequestPayload(channelName, buf));
	}

	@Override
	public void sendPacket(Packet<?> packet, PacketSendListener callback) {
		Objects.requireNonNull(packet, "Packet cannot be null");

		this.connection.send(packet, callback);
	}

	@Override
	public void disconnect(Component disconnectReason) {
		Objects.requireNonNull(disconnectReason, "Disconnect reason cannot be null");

		this.connection.disconnect(disconnectReason);
	}

	public void registerOutgoingPacket(ClientboundCustomQueryPacket packet) {
		this.channels.put(packet.transactionId(), packet.payload().id());
	}

	@Override
	protected void handleRegistration(ResourceLocation channelName) {
	}

	@Override
	protected void handleUnregistration(ResourceLocation channelName) {
	}

	@Override
	protected void invokeDisconnectEvent() {
		ServerLoginConnectionEvents.DISCONNECT.invoker().onLoginDisconnect(this.handler, this.server);
	}

	@Override
	protected boolean isReservedChannel(ResourceLocation channelName) {
		return false;
	}
}

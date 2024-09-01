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
package com.bookkeepersmc.notebook.api.client.networking.v1;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.networking.v1.ServerLoginNetworking;
import com.bookkeepersmc.notebook.impl.networking.client.ClientNetworkingImpl;

/**
 * Offers access to login stage client-side networking functionalities.
 *
 * <p>The Minecraft login protocol only allows the client to respond to a server's request, but not initiate one of its own.
 *
 * @see ClientPlayNetworking
 * @see ServerLoginNetworking
 */
public final class ClientLoginNetworking {
	/**
	 * Registers a handler to a query request channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterGlobalReceiver(Identifier)} to unregister the existing handler.
	 *
	 * @param channelName the id of the channel
	 * @param queryHandler the handler
	 * @return false if a handler is already registered to the channel
	 * @see ClientLoginNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ClientLoginNetworking#registerReceiver(Identifier, LoginQueryRequestHandler)
	 */
	public static boolean registerGlobalReceiver(Identifier channelName, LoginQueryRequestHandler queryHandler) {
		return ClientNetworkingImpl.LOGIN.registerGlobalReceiver(channelName, queryHandler);
	}

	/**
	 * Removes the handler of a query request channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ClientLoginNetworking#registerGlobalReceiver(Identifier, LoginQueryRequestHandler)
	 * @see ClientLoginNetworking#unregisterReceiver(Identifier)
	 */
	@Nullable
	public static ClientLoginNetworking.LoginQueryRequestHandler unregisterGlobalReceiver(Identifier channelName) {
		return ClientNetworkingImpl.LOGIN.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Gets all query request channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for.
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return ClientNetworkingImpl.LOGIN.getChannels();
	}

	/**
	 * Registers a handler to a query request channel.
	 *
	 * <p>If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(Identifier)} to unregister the existing handler.
	 *
	 * @param channelName the id of the channel
	 * @param queryHandler the handler
	 * @return false if a handler is already registered to the channel name
	 * @throws IllegalStateException if the client is not logging in
	 */
	public static boolean registerReceiver(Identifier channelName, LoginQueryRequestHandler queryHandler) throws IllegalStateException {
		final ClientConnection connection = ClientNetworkingImpl.getLoginConnection();

		if (connection != null) {
			final PacketListener packetListener = connection.getPacketListener();

			if (packetListener instanceof ClientLoginNetworkHandler) {
				return ClientNetworkingImpl.getAddon(((ClientLoginNetworkHandler) packetListener)).registerChannel(channelName, queryHandler);
			}
		}

		throw new IllegalStateException("Cannot register receiver while client is not logging in!");
	}

	/**
	 * Removes the handler of a query request channel.
	 *
	 * <p>The {@code channelName} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel name
	 * @throws IllegalStateException if the client is not logging in
	 */
	@Nullable
	public static LoginQueryRequestHandler unregisterReceiver(Identifier channelName) throws IllegalStateException {
		final ClientConnection connection = ClientNetworkingImpl.getLoginConnection();

		if (connection != null) {
			final PacketListener packetListener = connection.getPacketListener();

			if (packetListener instanceof ClientLoginNetworkHandler) {
				return ClientNetworkingImpl.getAddon(((ClientLoginNetworkHandler) packetListener)).unregisterChannel(channelName);
			}
		}

		throw new IllegalStateException("Cannot unregister receiver while client is not logging in!");
	}

	private ClientLoginNetworking() {
	}

	@FunctionalInterface
	public interface LoginQueryRequestHandler {
		/**
		 * Handles an incoming query request from a server.
		 *
		 * <p>This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft client instance.
		 *
		 * <p>The return value of this method is a completable future that may be used to delay the login process to the server until a task {@link CompletableFuture#isDone() is done}.
		 * The future should complete in reasonably time to prevent disconnection by the server.
		 * If your request processes instantly, you may use {@link CompletableFuture#completedFuture(Object)} to wrap your response for immediate sending.
		 *
		 * @param client the client
		 * @param handler the network handler that received this packet
		 * @param buf the payload of the packet
		 * @param callbacksConsumer listeners to be called when the response packet is sent to the server
		 * @return a completable future which contains the payload to respond to the server with.
		 * If the future contains {@code null}, then the server will be notified that the client did not understand the query.
		 */
		CompletableFuture<@Nullable PacketByteBuf> receive(Minecraft client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<PacketSendListener> callbacksConsumer);
	}
}

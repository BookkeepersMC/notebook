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
package com.bookkeepersmc.notebook.api.networking.v1;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

import com.bookkeepersmc.notebook.impl.networking.server.ServerNetworkingImpl;
import com.bookkeepersmc.notebook.mixin.networking.accessor.ServerCommonNetworkHandlerAccessor;

/**
 * Offers access to configuration stage server-side networking functionalities.
 *
 * <p>Server-side networking functionalities include receiving serverbound packets, sending clientbound packets, and events related to server-side network handlers.
 * Packets <strong>received</strong> by this class must be registered to {@link PayloadTypeRegistry#configurationC2S()} on both ends.
 * Packets <strong>sent</strong> by this class must be registered to {@link PayloadTypeRegistry#configurationS2C()} on both ends.
 * Packets must be registered before registering any receivers.
 *
 * <p>This class should be only used for the logical server.
 *
 * <p>See {@link ServerPlayNetworking} for information on sending and receiving play phase packets.
 *
 * <p>See the documentation on each class for more information.
 *
 * @see ServerLoginNetworking
 * @see ServerConfigurationNetworking
 */
public final class ServerConfigurationNetworking {
	/**
	 * Registers a handler for a payload type.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel
	 * @throws IllegalArgumentException if the codec for {@code type} has not been {@linkplain PayloadTypeRegistry#configurationC2S() registered} yet
	 * @see ServerConfigurationNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationNetworkHandler, CustomPayload.Id, ConfigurationPacketHandler)
	 */
	public static <T extends CustomPayload> boolean registerGlobalReceiver(CustomPayload.Id<T> type, ConfigurationPacketHandler<T> handler) {
		return ServerNetworkingImpl.CONFIGURATION.registerGlobalReceiver(type.id(), handler);
	}

	/**
	 * Removes the handler for a payload type.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code type} is guaranteed not to have an associated handler after this call.
	 *
	 * @param id the packet payload id
	 * @return the previous handler, or {@code null} if no handler was bound to the channel,
	 * or it was not registered using {@link #registerGlobalReceiver(CustomPayload.Id, ConfigurationPacketHandler)}
	 * @see ServerConfigurationNetworking#registerGlobalReceiver(CustomPayload.Id, ConfigurationPacketHandler)
	 * @see ServerConfigurationNetworking#unregisterReceiver(ServerConfigurationNetworkHandler, Identifier)
	 */
	@Nullable
	public static ServerConfigurationNetworking.ConfigurationPacketHandler<?> unregisterGlobalReceiver(Identifier id) {
		return ServerNetworkingImpl.CONFIGURATION.unregisterGlobalReceiver(id);
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for.
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return ServerNetworkingImpl.CONFIGURATION.getChannels();
	}

	/**
	 * Registers a handler for a payload type.
	 * This method differs from {@link ServerConfigurationNetworking#registerGlobalReceiver(CustomPayload.Id, ConfigurationPacketHandler)} since
	 * the channel handler will only be applied to the client represented by the {@link ServerConfigurationNetworkHandler}.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * @param networkHandler the network handler
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel name
	 * @throws IllegalArgumentException if the codec for {@code type} has not been {@linkplain PayloadTypeRegistry#configurationC2S() registered} yet
	 * @see ServerPlayConnectionEvents#INIT
	 */
	public static <T extends CustomPayload> boolean registerReceiver(ServerConfigurationNetworkHandler networkHandler, CustomPayload.Id<T> type, ConfigurationPacketHandler<T> handler) {
		return ServerNetworkingImpl.getAddon(networkHandler).registerChannel(type.id(), handler);
	}

	/**
	 * Removes the handler for a payload type.
	 *
	 * <p>The {@code type} is guaranteed not to have an associated handler after this call.
	 *
	 * @param id the id of the payload
	 * @return the previous handler, or {@code null} if no handler was bound to the channel,
	 * or it was not registered using {@link #registerReceiver(ServerConfigurationNetworkHandler, CustomPayload.Id, ConfigurationPacketHandler)}
	 */
	@Nullable
	public static ServerConfigurationNetworking.ConfigurationPacketHandler<?> unregisterReceiver(ServerConfigurationNetworkHandler networkHandler, Identifier id) {
		return ServerNetworkingImpl.getAddon(networkHandler).unregisterChannel(id);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param handler the network handler
	 * @return All the channel names that the server can receive packets on
	 */
	public static Set<Identifier> getReceived(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server configuration network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getReceivableChannels();
	}

	/**
	 * Gets all channel names that a connected client declared the ability to receive a packets on.
	 *
	 * @param handler the network handler
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static Set<Identifier> getSendable(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server configuration network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels();
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param handler the network handler
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static boolean canSend(ServerConfigurationNetworkHandler handler, Identifier channelName) {
		Objects.requireNonNull(handler, "Server configuration network handler cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(channelName);
	}

	/**
	 * Checks if the connected client declared the ability to receive a specific type of packet.
	 *
	 * @param handler the network handler
	 * @param id the payload id
	 * @return {@code true} if the connected client has declared the ability to receive a specific type of packet
	 */
	public static boolean canSend(ServerConfigurationNetworkHandler handler, CustomPayload.Id<?> id) {
		Objects.requireNonNull(handler, "Server configuration network handler cannot be null");
		Objects.requireNonNull(id, "Payload id cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(id.id());
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param payload the payload
	 * @return a new packet
	 */
	public static Packet<ClientCommonPacketListener> createS2CPacket(CustomPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.getId(), "CustomPayload#getId() cannot return null for payload class: " + payload.getClass());

		return ServerNetworkingImpl.createS2CPacket(payload);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param handler the network handler, representing the connection to the player/client
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server configuration network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler);
	}

	/**
	 * Sends a packet to a configuring player.
	 *
	 * <p>Any packets sent must be {@linkplain PayloadTypeRegistry#configurationS2C() registered}.</p>
	 *
	 * @param handler the network handler to send the packet to
	 * @param payload to be sent
	 */
	public static void send(ServerConfigurationNetworkHandler handler, CustomPayload payload) {
		Objects.requireNonNull(handler, "Server configuration handler cannot be null");
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.getId(), "CustomPayload#getId() cannot return null for payload class: " + payload.getClass());

		handler.send(createS2CPacket(payload));
	}

	// Helper methods

	/**
	 * Returns the <i>Minecraft</i> Server of a server configuration network handler.
	 *
	 * @param handler the server configuration network handler
	 */
	public static MinecraftServer getServer(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Network handler cannot be null");

		return ((ServerCommonNetworkHandlerAccessor) handler).getServer();
	}

	private ServerConfigurationNetworking() {
	}

	/**
	 * A packet handler utilizing {@link CustomPayload}.
	 * @param <T> the type of the packet
	 */
	@FunctionalInterface
	public interface ConfigurationPacketHandler<T extends CustomPayload> {
		/**
		 * Handles an incoming packet.
		 *
		 * <p>Unlike {@link ServerPlayNetworking.PlayPayloadHandler} this method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain ThreadExecutor#submit(Runnable) scheduled} using the Minecraft server instance from {@link ServerConfigurationNetworking#getServer(ServerConfigurationNetworkHandler)}.
		 *
		 * <p>An example usage of this:
		 * <pre>{@code
		 * // use PayloadTypeRegistry for registering the payload
		 * ServerConfigurationNetworking.registerReceiver(BOOM_PACKET_TYPE, (payload, context) -> {
		 *
		 * });
		 * }</pre>
		 *
		 *
		 * @param payload the packet payload
		 * @param context the configuration networking context
		 * @see CustomPayload
		 */
		void receive(T payload, Context context);
	}

	@ApiStatus.NonExtendable
	public interface Context {
		/**
		 * @return The MinecraftServer instance
		 */
		MinecraftServer server();

		/**
		 * @return The ServerConfigurationNetworkHandler instance
		 */
		ServerConfigurationNetworkHandler networkHandler();

		/**
		 * @return The packet sender
		 */
		PacketSender responseSender();
	}
}

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

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.bookkeepersmc.notebook.impl.networking.server.ServerNetworkingImpl;

/**
 * Offers access to play stage server-side networking functionalities.
 *
 * <p>Server-side networking functionalities include receiving serverbound packets, sending clientbound packets, and events related to server-side network handlers.
 * Packets <strong>received</strong> by this class must be registered to {@link PayloadTypeRegistry#playC2S()} on both ends.
 * Packets <strong>sent</strong> by this class must be registered to {@link PayloadTypeRegistry#playS2C()} on both ends.
 * Packets must be registered before registering any receivers.
 *
 * <p>This class should be only used for the logical server.
 *
 * <h2>Packet object-based API</h2>
 *
 * <p>This class provides a registration method, utilizing packet objects, {@link #registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)}.
 * This handler executes the callback in the server thread, ensuring thread safety.
 *
 * <p>This payload object-based API involves three classes:
 *
 * <ul>
 *     <li>A class implementing {@link CustomPacketPayload} that is "sent" over the network</li>
 *     <li>{@link CustomPacketPayload.TypeAndCodec} instance, which represents the packet's type (and its codec)</li>
 *     <li>{@link PlayPayloadHandler}, which handles the packet (usually implemented as a functional interface)</li>
 * </ul>
 *
 * <p>See the documentation on each class for more information.
 *
 * @see ServerLoginNetworking
 * @see ServerConfigurationNetworking
 */
public final class ServerPlayNetworking {
	/**
	 * Registers a handler for a payload type.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterGlobalReceiver(ResourceLocation)} to unregister the existing handler.
	 *
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel
	 * @throws IllegalArgumentException if the codec for {@code type} has not been {@linkplain PayloadTypeRegistry#playC2S() registered} yet
	 * @see ServerPlayNetworking#unregisterGlobalReceiver(ResourceLocation)
	 */
	public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
		return ServerNetworkingImpl.PLAY.registerGlobalReceiver(type.id(), handler);
	}

	/**
	 * Removes the handler for a payload type.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code id} is guaranteed not to have an associated handler after this call.
	 *
	 * @param id the payload id
	 * @return the previous handler, or {@code null} if no handler was bound to the channel,
	 * or it was not registered using {@link #registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)}
	 * @see ServerPlayNetworking#registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)
	 * @see ServerPlayNetworking#unregisterReceiver(ServerGamePacketListenerImpl, ResourceLocation)
	 */
	@Nullable
	public static ServerPlayNetworking.PlayPayloadHandler<?> unregisterGlobalReceiver(ResourceLocation id) {
		return ServerNetworkingImpl.PLAY.unregisterGlobalReceiver(id);
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for.
	 */
	public static Set<ResourceLocation> getGlobalReceivers() {
		return ServerNetworkingImpl.PLAY.getChannels();
	}

	/**
	 * Registers a handler for a payload type.
	 * This method differs from {@link ServerPlayNetworking#registerGlobalReceiver(CustomPacketPayload.Type, PlayPayloadHandler)} since
	 * the channel handler will only be applied to the player represented by the {@link ServerGamePacketListenerImpl}.
	 *
	 * <p>For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(ResourceLocation, ServerLoginNetworking.LoginQueryResponseHandler)}
	 * login response has been received, you should use {@link ServerPlayConnectionEvents#INIT} to register the channel handler.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerGamePacketListenerImpl, ResourceLocation)} to unregister the existing handler.
	 *
	 * @param networkHandler the network handler
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel name
	 * @throws IllegalArgumentException if the codec for {@code type} has not been {@linkplain PayloadTypeRegistry#playC2S() registered} yet
	 * @see ServerPlayConnectionEvents#INIT
	 */
	public static <T extends CustomPacketPayload> boolean registerReceiver(ServerGamePacketListenerImpl networkHandler, CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
		return ServerNetworkingImpl.getAddon(networkHandler).registerChannel(type.id(), handler);
	}

	/**
	 * Removes the handler for a packet type.
	 *
	 * <p>The {@code id} is guaranteed not to have an associated handler after this call.
	 *
	 * @param id the id of the payload
	 * @return the previous handler, or {@code null} if no handler was bound to the channel,
	 * or it was not registered using {@link #registerReceiver(ServerGamePacketListenerImpl, CustomPacketPayload.Type, PlayPayloadHandler)}
	 */
	@Nullable
	public static ServerPlayNetworking.PlayPayloadHandler<?> unregisterReceiver(ServerGamePacketListenerImpl networkHandler, ResourceLocation id) {
		return ServerNetworkingImpl.getAddon(networkHandler).unregisterChannel(id);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param player the player
	 * @return All the channel names that the server can receive packets on
	 */
	public static Set<ResourceLocation> getReceived(ServerPlayer player) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return getReceived(player.connection);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param handler the network handler
	 * @return All the channel names that the server can receive packets on
	 */
	public static Set<ResourceLocation> getReceived(ServerGamePacketListenerImpl handler) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getReceivableChannels();
	}

	/**
	 * Gets all channel names that the connected client declared the ability to receive a packets on.
	 *
	 * @param player the player
	 * @return All the channel names the connected client declared the ability to receive a packets on
	 */
	public static Set<ResourceLocation> getSendable(ServerPlayer player) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return getSendable(player.connection);
	}

	/**
	 * Gets all channel names that a connected client declared the ability to receive a packets on.
	 *
	 * @param handler the network handler
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static Set<ResourceLocation> getSendable(ServerGamePacketListenerImpl handler) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels();
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param player the player
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static boolean canSend(ServerPlayer player, ResourceLocation channelName) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return canSend(player.connection, channelName);
	}

	/**
	 * Checks if the connected client declared the ability to receive a specific type of packet.
	 *
	 * @param player the player
	 * @param type the packet type
	 * @return {@code true} if the connected client has declared the ability to receive a specific type of packet
	 */
	public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return canSend(player.connection, type.id());
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param handler the network handler
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static boolean canSend(ServerGamePacketListenerImpl handler, ResourceLocation channelName) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(channelName);
	}

	/**
	 * Checks if the connected client declared the ability to receive a specific type of packet.
	 *
	 * @param handler the network handler
	 * @param type the packet type
	 * @return {@code true} if the connected client has declared the ability to receive a specific type of packet
	 */
	public static boolean canSend(ServerGamePacketListenerImpl handler, CustomPacketPayload.Type<?> type) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");
		Objects.requireNonNull(type, "Packet type cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(type.id());
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param packet the packet
	 * @return a new packet
	 */
	public static <T extends CustomPacketPayload> Packet<ClientCommonPacketListener> createS2CPacket(T packet) {
		return ServerNetworkingImpl.createS2CPacket(packet);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param player the player
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerPlayer player) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return getSender(player.connection);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param handler the network handler, representing the connection to the player/client
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerGamePacketListenerImpl handler) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler);
	}

	/**
	 * Sends a packet to a player.
	 *
	 * <p>Any packets sent must be {@linkplain PayloadTypeRegistry#playS2C() registered}.</p>
	 *
	 * @param player the player to send the packet to
	 * @param payload the payload to send
	 */
	public static void send(ServerPlayer player, CustomPacketPayload payload) {
		Objects.requireNonNull(player, "Server player entity cannot be null");
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.type(), "CustomPayload#getId() cannot return null for payload class: " + payload.getClass());

		player.connection.send(createS2CPacket(payload));
	}

	private ServerPlayNetworking() {
	}

	/**
	 * A thread-safe packet handler utilizing {@link CustomPacketPayload}.
	 * @param <T> the type of the packet
	 */
	@FunctionalInterface
	public interface PlayPayloadHandler<T extends CustomPacketPayload> {
		/**
		 * Handles the incoming packet. This is called on the server thread, and can safely
		 * manipulate the world.
		 *
		 * <p>An example usage of this is to create an explosion where the player is looking:
		 * <pre>{@code
		 * // use PayloadTypeRegistry for registering the payload
		 * ServerPlayNetworking.registerReceiver(BoomPayload.ID, (payload, context) -> {
		 * 	ModPacketHandler.createExplosion(context.player(), payload.fire());
		 * });
		 * }</pre>
		 *
		 * <p>The network handler can be accessed via {@link ServerPlayer#connection}.
		 *
		 * @param payload the packet payload
		 * @param context the play networking context
		 * @see CustomPacketPayload
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
		 * @return The player that received the packet
		 */
		ServerPlayer player();

		/**
		 * @return The packet sender
		 */
		PacketSender responseSender();
	}
}

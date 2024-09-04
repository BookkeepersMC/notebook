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

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.payload.CustomPayload;

import com.bookkeepersmc.notebook.api.client.networking.v1.ClientConfigurationConnectionEvents;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientConfigurationNetworking;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientLoginNetworking;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientPlayConnectionEvents;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientPlayNetworking;
import com.bookkeepersmc.notebook.api.networking.v1.PacketSender;
import com.bookkeepersmc.notebook.impl.networking.CommonPacketsImpl;
import com.bookkeepersmc.notebook.impl.networking.CommonRegisterPayload;
import com.bookkeepersmc.notebook.impl.networking.CommonVersionPayload;
import com.bookkeepersmc.notebook.impl.networking.GlobalReceiverRegistry;
import com.bookkeepersmc.notebook.impl.networking.NetworkHandlerExtensions;
import com.bookkeepersmc.notebook.impl.networking.NetworkingImpl;
import com.bookkeepersmc.notebook.impl.networking.PayloadTypeRegistryImpl;
import com.bookkeepersmc.notebook.mixin.networking.client.accessor.ConnectScreenAccessor;
import com.bookkeepersmc.notebook.mixin.networking.client.accessor.MinecraftAccessor;

public final class ClientNetworkingImpl {
	public static final GlobalReceiverRegistry<ClientLoginNetworking.LoginQueryRequestHandler> LOGIN = new GlobalReceiverRegistry<>(NetworkSide.S2C, NetworkPhase.LOGIN, null);
	public static final GlobalReceiverRegistry<ClientConfigurationNetworking.ConfigurationPayloadHandler<?>> CONFIGURATION = new GlobalReceiverRegistry<>(NetworkSide.S2C, NetworkPhase.CONFIGURATION, PayloadTypeRegistryImpl.CONFIGURATION_S2C);
	public static final GlobalReceiverRegistry<ClientPlayNetworking.PlayPayloadHandler<?>> PLAY = new GlobalReceiverRegistry<>(NetworkSide.S2C, NetworkPhase.PLAY, PayloadTypeRegistryImpl.PLAY_S2C);

	private static ClientPlayNetworkAddon currentPlayAddon;
	private static ClientConfigurationNetworkAddon currentConfigurationAddon;

	public static ClientPlayNetworkAddon getAddon(ClientPlayNetworkHandler handler) {
		return (ClientPlayNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ClientConfigurationNetworkAddon getAddon(ClientConfigurationNetworkHandler handler) {
		return (ClientConfigurationNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ClientLoginNetworkAddon getAddon(ClientLoginNetworkHandler handler) {
		return (ClientLoginNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static Packet<ServerCommonPacketListener> createC2SPacket(CustomPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");
		Objects.requireNonNull(payload.getId(), "CustomPayload#getId() cannot return null for payload class: " + payload.getClass());

		return new CustomPayloadC2SPacket(payload);
	}

	/**
	 * Due to the way logging into an integrated or remote dedicated server will differ, we need to obtain the login client connection differently.
	 */
	@Nullable
	public static ClientConnection getLoginConnection() {
		final ClientConnection connection = ((MinecraftAccessor) Minecraft.getInstance()).getConnection();

		// Check if we are connecting to an integrated server. This will set the field on Minecraft
		if (connection != null) {
			return connection;
		} else {
			// We are probably connecting to a remote server.
			// Check if the ConnectScreen is the currentScreen to determine that:
			if (Minecraft.getInstance().currentScreen instanceof ConnectScreen) {
				return ((ConnectScreenAccessor) Minecraft.getInstance().currentScreen).getConnection();
			}
		}

		// We are not connected to a server at all.
		return null;
	}

	@Nullable
	public static ClientConfigurationNetworkAddon getClientConfigurationAddon() {
		return currentConfigurationAddon;
	}

	@Nullable
	public static ClientPlayNetworkAddon getClientPlayAddon() {
		// Since Minecraft can be a bit weird, we need to check for the play addon in a few ways:
		// If the client's player is set this will work
		if (Minecraft.getInstance().getNetworkHandler() != null) {
			currentPlayAddon = null; // Shouldn't need this anymore
			return getAddon(Minecraft.getInstance().getNetworkHandler());
		}

		// We haven't hit the end of onGameJoin yet, use our backing field here to access the network handler
		if (currentPlayAddon != null) {
			return currentPlayAddon;
		}

		// We are not in play stage
		return null;
	}

	public static void setClientPlayAddon(ClientPlayNetworkAddon addon) {
		assert addon == null || currentConfigurationAddon == null;
		currentPlayAddon = addon;
	}

	public static void setClientConfigurationAddon(ClientConfigurationNetworkAddon addon) {
		currentConfigurationAddon = addon;
	}

	public static void clientInit() {
		// Reference cleanup for the locally stored addon if we are disconnected
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			currentPlayAddon = null;
		});

		ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> {
			currentConfigurationAddon = null;
		});

		// Version packet
		ClientConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.ID, (payload, context) -> {
			int negotiatedVersion = handleVersionPacket(payload, context.responseSender());
			ClientNetworkingImpl.getClientConfigurationAddon().onCommonVersionPacket(negotiatedVersion);
		});

		// Register packet
		ClientConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.ID, (payload, context) -> {
			ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getClientConfigurationAddon();

			if (CommonRegisterPayload.PLAY_PHASE.equals(payload.phase())) {
				if (payload.version() != addon.getNegotiatedVersion()) {
					throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), payload.version()));
				}

				addon.getChannelInfoHolder().notebook_getPendingChannelsNames(NetworkPhase.PLAY).addAll(payload.channels());
				NetworkingImpl.LOGGER.debug("Received accepted channels from the server");
				context.responseSender().sendPacket(new CommonRegisterPayload(addon.getNegotiatedVersion(), CommonRegisterPayload.PLAY_PHASE, ClientPlayNetworking.getGlobalReceivers()));
			} else {
				addon.onCommonRegisterPacket(payload);
				context.responseSender().sendPacket(addon.createRegisterPayload());
			}
		});
	}

	// Disconnect if there are no commonly supported versions.
	// Client responds with the intersection of supported versions.
	// Return the highest supported version
	private static int handleVersionPacket(CommonVersionPayload payload, PacketSender packetSender) {
		int version = CommonPacketsImpl.getHighestCommonVersion(payload.versions(), CommonPacketsImpl.SUPPORTED_COMMON_PACKET_VERSIONS);

		if (version <= 0) {
			throw new UnsupportedOperationException("Client does not support any requested versions from server");
		}

		packetSender.sendPacket(new CommonVersionPayload(new int[]{ version }));
		return version;
	}
}

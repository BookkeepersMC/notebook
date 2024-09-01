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
package com.bookkeepersmc.notebook.impl.networking;

import java.util.Arrays;
import java.util.function.Consumer;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;

import com.bookkeepersmc.notebook.api.networking.v1.PayloadTypeRegistry;
import com.bookkeepersmc.notebook.api.networking.v1.ServerConfigurationConnectionEvents;
import com.bookkeepersmc.notebook.api.networking.v1.ServerConfigurationNetworking;
import com.bookkeepersmc.notebook.api.networking.v1.ServerPlayNetworking;
import com.bookkeepersmc.notebook.impl.networking.server.ServerConfigurationNetworkAddon;
import com.bookkeepersmc.notebook.impl.networking.server.ServerNetworkingImpl;

public class CommonPacketsImpl {
	public static final int PACKET_VERSION_1 = 1;
	public static final int[] SUPPORTED_COMMON_PACKET_VERSIONS = new int[]{ PACKET_VERSION_1 };

	public static void init() {
		PayloadTypeRegistry.configurationC2S().register(CommonVersionPayload.ID, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(CommonVersionPayload.ID, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CommonVersionPayload.ID, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CommonVersionPayload.ID, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.configurationC2S().register(CommonRegisterPayload.ID, CommonRegisterPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(CommonRegisterPayload.ID, CommonRegisterPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CommonRegisterPayload.ID, CommonRegisterPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CommonRegisterPayload.ID, CommonRegisterPayload.CODEC);

		ServerConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.ID, (payload, context) -> {
			ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(context.networkHandler());
			addon.onCommonVersionPacket(getNegotiatedVersion(payload));
			context.networkHandler().completeTask(CommonVersionConfigurationTask.KEY);
		});

		ServerConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.ID, (payload, context) -> {
			ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(context.networkHandler());

			if (CommonRegisterPayload.PLAY_PHASE.equals(payload.phase())) {
				if (payload.version() != addon.getNegotiatedVersion()) {
					throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), payload.version()));
				}

				// Play phase hasnt started yet, add them to the pending names.
				addon.getChannelInfoHolder().fabric_getPendingChannelsNames(NetworkPhase.PLAY).addAll(payload.channels());
				NetworkingImpl.LOGGER.debug("Received accepted channels from the client for play phase");
			} else {
				addon.onCommonRegisterPacket(payload);
			}

			context.networkHandler().completeTask(CommonRegisterConfigurationTask.KEY);
		});

		// Create a configuration task to send and receive the common packets
		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			final ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(handler);

			if (ServerConfigurationNetworking.canSend(handler, CommonVersionPayload.ID)) {
				// Tasks are processed in order.
				handler.addTask(new CommonVersionConfigurationTask(addon));

				if (ServerConfigurationNetworking.canSend(handler, CommonRegisterPayload.ID)) {
					handler.addTask(new CommonRegisterConfigurationTask(addon));
				}
			}
		});
	}

	// A configuration phase task to send and receive the version packets.
	private record CommonVersionConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask {
		public static final Type KEY = new Type(CommonVersionPayload.ID.id().toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			addon.sendPacket(new CommonVersionPayload(SUPPORTED_COMMON_PACKET_VERSIONS));
		}

		@Override
		public Type getType() {
			return KEY;
		}
	}

	// A configuration phase task to send and receive the registration packets.
	private record CommonRegisterConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask {
		public static final Type KEY = new Type(CommonRegisterPayload.ID.id().toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			addon.sendPacket(new CommonRegisterPayload(addon.getNegotiatedVersion(), CommonRegisterPayload.PLAY_PHASE, ServerPlayNetworking.getGlobalReceivers()));
		}

		@Override
		public Type getType() {
			return KEY;
		}
	}

	private static int getNegotiatedVersion(CommonVersionPayload payload) {
		int version = getHighestCommonVersion(payload.versions(), SUPPORTED_COMMON_PACKET_VERSIONS);

		if (version <= 0) {
			throw new UnsupportedOperationException("server does not support any requested versions from client");
		}

		return version;
	}

	public static int getHighestCommonVersion(int[] a, int[] b) {
		int[] as = a.clone();
		int[] bs = b.clone();

		Arrays.sort(as);
		Arrays.sort(bs);

		int ap = as.length - 1;
		int bp = bs.length - 1;

		while (ap >= 0 && bp >= 0) {
			if (as[ap] == bs[bp]) {
				return as[ap];
			}

			if (as[ap] > bs[bp]) {
				ap--;
			} else {
				bp--;
			}
		}

		return -1;
	}
}

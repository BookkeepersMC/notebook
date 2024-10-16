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

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.payload.CustomPayload;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;

/**
 * Offers access to events related to the configuration connection to a server on a logical client.
 */
public final class ClientConfigurationConnectionEvents {
	/**
	 * Event indicating a connection entering the CONFIGURATION state, ready for registering channel handlers.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 *
	 * @see ClientConfigurationNetworking#registerReceiver(CustomPayload.Id, ClientConfigurationNetworking.ConfigurationPayloadHandler)
	 */
	public static final Event<ClientConfigurationConnectionEvents.Init> INIT = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Init.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Init callback : callbacks) {
			callback.onConfigurationInit(handler, client);
		}
	});

	/**
	 * An event called after the connection has been initialized and is ready to start sending and receiving configuration packets.
	 *
	 * <p>Packets may be sent during this event.
	 */
	public static final Event<ClientConfigurationConnectionEvents.Start> START = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Start.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Start callback : callbacks) {
			callback.onConfigurationStart(handler, client);
		}
	});

	/**
	 * An event called after the ReadyS2CPacket has been received, just before switching to the PLAY state.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<ClientConfigurationConnectionEvents.Complete> COMPLETE = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Complete.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Complete callback : callbacks) {
			callback.onConfigurationComplete(handler, client);
		}
	});

	/**
	 * An event for the disconnection of the client configuration network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<ClientConfigurationConnectionEvents.Disconnect> DISCONNECT = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Disconnect.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Disconnect callback : callbacks) {
			callback.onConfigurationDisconnect(handler, client);
		}
	});

	private ClientConfigurationConnectionEvents() {
	}

	@FunctionalInterface
	public interface Init {
		void onConfigurationInit(ClientConfigurationNetworkHandler handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Start {
		void onConfigurationStart(ClientConfigurationNetworkHandler handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Complete {
		void onConfigurationComplete(ClientConfigurationNetworkHandler handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onConfigurationDisconnect(ClientConfigurationNetworkHandler handler, Minecraft client);
	}

	// Deprecated:

	/**
	 * @deprecated replaced by {@link #COMPLETE}
	 */
	@Deprecated
	public static final Event<ClientConfigurationConnectionEvents.Ready> READY = EventFactory.createArrayBacked(ClientConfigurationConnectionEvents.Ready.class, callbacks -> (handler, client) -> {
		for (ClientConfigurationConnectionEvents.Ready callback : callbacks) {
			callback.onConfigurationReady(handler, client);
		}
	});

	/**
	 * @deprecated replaced by {@link ClientConfigurationConnectionEvents.Complete}
	 */
	@Deprecated
	@FunctionalInterface
	public interface Ready {
		void onConfigurationReady(ClientConfigurationNetworkHandler handler, Minecraft client);
	}
}

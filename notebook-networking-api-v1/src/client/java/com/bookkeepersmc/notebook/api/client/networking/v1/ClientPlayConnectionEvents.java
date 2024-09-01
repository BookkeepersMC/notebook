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
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.payload.CustomPayload;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;
import com.bookkeepersmc.notebook.api.networking.v1.PacketSender;

/**
 * Offers access to events related to the connection to a server on a logical client.
 */
public final class ClientPlayConnectionEvents {
	/**
	 * Event indicating a connection entered the PLAY state, ready for registering channel handlers.
	 *
	 * @see ClientPlayNetworking#registerReceiver(CustomPayload.Id, ClientPlayNetworking.PlayPayloadHandler)
	 */
	public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, callbacks -> (handler, client) -> {
		for (Init callback : callbacks) {
			callback.onPlayInit(handler, client);
		}
	});

	/**
	 * An event for notification when the client play network handler is ready to send packets to the server.
	 *
	 * <p>At this stage, the network handler is ready to send packets to the server.
	 * Since the client's local state has been set up.
	 */
	public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, client) -> {
		for (Join callback : callbacks) {
			callback.onPlayReady(handler, sender, client);
		}
	});

	/**
	 * An event for the disconnection of the client play network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, client) -> {
		for (Disconnect callback : callbacks) {
			callback.onPlayDisconnect(handler, client);
		}
	});

	private ClientPlayConnectionEvents() {
	}

	@FunctionalInterface
	public interface Init {
		void onPlayInit(ClientPlayNetworkHandler handler, Minecraft client);
	}

	@FunctionalInterface
	public interface Join {
		void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, Minecraft client);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onPlayDisconnect(ClientPlayNetworkHandler handler, Minecraft client);
	}
}

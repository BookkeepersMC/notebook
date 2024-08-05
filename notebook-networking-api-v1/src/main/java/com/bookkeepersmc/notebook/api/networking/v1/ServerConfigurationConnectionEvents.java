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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;

/**
 * Offers access to events related to the connection to a client on a logical server while a client is configuring.
 */
public final class ServerConfigurationConnectionEvents {
	/**
	 * Event fired before any vanilla configuration has taken place.
	 *
	 * <p>This event is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
	 *
	 * <p>Task queued during this event will complete before vanilla configuration starts.
	 */
	public static final Event<Configure> BEFORE_CONFIGURE = EventFactory.createArrayBacked(Configure.class, callbacks -> (handler, server) -> {
		for (Configure callback : callbacks) {
			callback.onSendConfiguration(handler, server);
		}
	});

	/**
	 * Event fired during vanilla configuration.
	 *
	 * <p>This event is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
	 *
	 * <p>An example usage of this:
	 * <pre>{@code
	 * ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
	 * 	if (ServerConfigurationNetworking.canSend(handler, ConfigurationPacket.PACKET_TYPE)) {
	 *  handler.addTask(new TestConfigurationTask("Example data"));
	 * 	} else {
	 * 	  // You can opt to disconnect the client if it cannot handle the configuration task
	 * 	  handler.disconnect(Text.literal("Network test configuration not supported by client"));
	 * 	  }
	 * });
	 * }</pre>
	 */
	public static final Event<Configure> CONFIGURE = EventFactory.createArrayBacked(Configure.class, callbacks -> (handler, server) -> {
		for (Configure callback : callbacks) {
			callback.onSendConfiguration(handler, server);
		}
	});

	/**
	 * An event for the disconnection of the server configuration network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = EventFactory.createArrayBacked(Disconnect.class, callbacks -> (handler, server) -> {
		for (Disconnect callback : callbacks) {
			callback.onConfigureDisconnect(handler, server);
		}
	});

	private ServerConfigurationConnectionEvents() {
	}

	@FunctionalInterface
	public interface Configure {
		void onSendConfiguration(ServerConfigurationPacketListenerImpl handler, MinecraftServer server);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onConfigureDisconnect(ServerConfigurationPacketListenerImpl handler, MinecraftServer server);
	}
}

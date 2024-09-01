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

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;
import com.bookkeepersmc.notebook.api.networking.v1.PacketSender;

/**
 * Offers access to events related to the indication of a connected server's ability to receive packets in certain channels.
 */
public final class C2SConfigurationChannelEvents {
	/**
	 * An event for the client configuration network handler receiving an update indicating the connected server's ability to receive packets in certain channels.
	 * This event may be invoked at any time after login and up to disconnection.
	 */
	public static final Event<Register> REGISTER = EventFactory.createArrayBacked(Register.class, callbacks -> (handler, sender, client, channels) -> {
		for (Register callback : callbacks) {
			callback.onChannelRegister(handler, sender, client, channels);
		}
	});

	/**
	 * An event for the client configuration network handler receiving an update indicating the connected server's lack of ability to receive packets in certain channels.
	 * This event may be invoked at any time after login and up to disconnection.
	 */
	public static final Event<Unregister> UNREGISTER = EventFactory.createArrayBacked(Unregister.class, callbacks -> (handler, sender, client, channels) -> {
		for (Unregister callback : callbacks) {
			callback.onChannelUnregister(handler, sender, client, channels);
		}
	});

	private C2SConfigurationChannelEvents() {
	}

	/**
	 * @see C2SConfigurationChannelEvents#REGISTER
	 */
	@FunctionalInterface
	public interface Register {
		void onChannelRegister(ClientConfigurationNetworkHandler handler, PacketSender sender, Minecraft client, List<Identifier> channels);
	}

	/**
	 * @see C2SConfigurationChannelEvents#UNREGISTER
	 */
	@FunctionalInterface
	public interface Unregister {
		void onChannelUnregister(ClientConfigurationNetworkHandler handler, PacketSender sender, Minecraft client, List<Identifier> channels);
	}
}

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

import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.AbstractClientNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.networking.AbstractChanneledNetworkAddon;
import com.bookkeepersmc.notebook.impl.networking.GlobalReceiverRegistry;
import com.bookkeepersmc.notebook.impl.networking.NetworkingImpl;
import com.bookkeepersmc.notebook.impl.networking.RegistrationPayload;

abstract class ClientCommonNetworkAddon<H, T extends AbstractClientNetworkHandler> extends AbstractChanneledNetworkAddon<H> {
	protected final T handler;
	protected final Minecraft client;

	protected boolean isServerReady = false;

	protected ClientCommonNetworkAddon(GlobalReceiverRegistry<H> receiver, ClientConnection connection, String description, T handler, Minecraft client) {
		super(receiver, connection, description);
		this.handler = handler;
		this.client = client;
	}

	public void onServerReady() {
		this.isServerReady = true;
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		// If we can already send packets, immediately send the register packet for this channel
		if (this.isServerReady) {
			final RegistrationPayload payload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName));

			if (payload != null) {
				this.sendPacket(payload);
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.isServerReady) {
			final RegistrationPayload payload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName));

			if (payload != null) {
				this.sendPacket(payload);
			}
		}
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}

	@Override
	protected void schedule(Runnable task) {
		client.execute(task);
	}
}

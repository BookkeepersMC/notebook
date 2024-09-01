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
package com.bookkeepersmc.notebook.impl.client.registry.sync;

import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.text.Text;

import com.bookkeepersmc.api.ClientModInitializer;
import com.bookkeepersmc.notebook.api.client.networking.v1.ClientConfigurationNetworking;
import com.bookkeepersmc.notebook.impl.registry.sync.RegistrySyncManager;
import com.bookkeepersmc.notebook.impl.registry.sync.RemapException;
import com.bookkeepersmc.notebook.impl.registry.sync.SyncCompletePayload;
import com.bookkeepersmc.notebook.impl.registry.sync.packet.RegistryPacketHandler;

public class RegistrySyncClientInit implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrySyncClientInit.class);

	@Override
	public void onInitializeClient() {
		registerSyncPacketReceiver(RegistrySyncManager.DIRECT_PACKET_HANDLER);
	}

	private <T extends RegistryPacketHandler.RegistrySyncPayload> void registerSyncPacketReceiver(RegistryPacketHandler<T> packetHandler) {
		ClientConfigurationNetworking.registerGlobalReceiver(packetHandler.getPacketId(), (payload, context) -> {
			RegistrySyncManager.receivePacket(context.client(), packetHandler, payload, RegistrySyncManager.DEBUG || !context.client().isSingleplayer())
					.whenComplete((complete, throwable) -> {
						if (throwable != null) {
							LOGGER.error("Registry remapping failed!", throwable);
							context.client().execute(() -> context.responseSender().disconnect(getText(throwable)));
							return;
						}

						if (complete) {
							context.responseSender().sendPacket(SyncCompletePayload.INSTANCE);
						}
					});
		});
	}

	private Text getText(Throwable e) {
		if (e instanceof RemapException remapException) {
			final Text text = remapException.getComponent();

			if (text != null) {
				return text;
			}
		} else if (e instanceof CompletionException completionException) {
			return getText(completionException.getCause());
		}

		return Text.literal("Registry remapping failed: " + e.getMessage());
	}
}

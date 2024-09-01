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
package com.bookkeepersmc.notebook.impl.recipe;

import java.util.Set;
import java.util.function.Consumer;

import io.netty.channel.ChannelHandler;

import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.api.ModInitializer;
import com.bookkeepersmc.notebook.api.networking.v1.PayloadTypeRegistry;
import com.bookkeepersmc.notebook.api.networking.v1.ServerConfigurationConnectionEvents;
import com.bookkeepersmc.notebook.api.networking.v1.ServerConfigurationNetworking;
import com.bookkeepersmc.notebook.mixin.networking.accessor.ServerCommonNetworkHandlerAccessor;

public class CustomIngredientSync implements ModInitializer {
	public static final Identifier PACKET_ID = Identifier.of("notebook", "custom_ingredient_sync");
	public static final int PROTOCOL_VERSION_1 = 1;
	public static final ThreadLocal<Set<Identifier>> CURRENT_SUPPORTED_INGREDIENTS = new ThreadLocal<>();

	public static CustomIngredientPayloadC2S createResponsePayload(int serverProtocolVersion) {
		if (serverProtocolVersion < PROTOCOL_VERSION_1) {
			return null;
		}

		return new CustomIngredientPayloadC2S(PROTOCOL_VERSION_1, CustomIngredientImpl.REGISTERED_SERIALIZERS.keySet());
	}

	public static Set<Identifier> decodeResponsePayload(CustomIngredientPayloadC2S payload) {
		int protocolVersion = payload.protocolVersion();
		switch (protocolVersion) {
			case PROTOCOL_VERSION_1 -> {
				Set<Identifier> serializers = payload.registeredSerializers();
				// Remove unknown keys to save memory
				serializers.removeIf(id -> !CustomIngredientImpl.REGISTERED_SERIALIZERS.containsKey(id));
				return serializers;
			}
			default -> {
				throw new IllegalArgumentException("Unknown ingredient sync protocol version: " + protocolVersion);
			}
		}
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationC2S()
				.register(CustomIngredientPayloadC2S.TYPE, CustomIngredientPayloadC2S.CODEC);
		PayloadTypeRegistry.configurationS2C()
				.register(CustomIngredientPayloadS2C.TYPE, CustomIngredientPayloadS2C.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.canSend(handler, PACKET_ID)) {
				handler.addTask(new IngredientSyncTask());
			}
		});

		ServerConfigurationNetworking.registerGlobalReceiver(CustomIngredientPayloadC2S.TYPE, (payload, context) -> {
			Set<Identifier> supportedCustomIngredients = decodeResponsePayload(payload);
			ChannelHandler packetEncoder = ((ServerCommonNetworkHandlerAccessor) context.networkHandler()).getConnection().channel.pipeline().get("encoder");

			if (packetEncoder != null) { // Null in singleplayer
				((SupportedIngredientsPacketEncoder) packetEncoder).notebook_setSupportedCustomIngredients(supportedCustomIngredients);
			}

			context.networkHandler().completeTask(IngredientSyncTask.KEY);
		});
	}

	private record IngredientSyncTask() implements ConfigurationTask {
		public static final Type KEY = new Type(PACKET_ID.toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			sender.accept(ServerConfigurationNetworking.createS2CPacket(new CustomIngredientPayloadS2C(PROTOCOL_VERSION_1)));
		}

		@Override
		public Type getType() {
			return KEY;
		}
	}
}

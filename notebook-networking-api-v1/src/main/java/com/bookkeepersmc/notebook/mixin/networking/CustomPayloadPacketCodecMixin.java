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
package com.bookkeepersmc.notebook.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.networking.CustomPayloadTypeProvider;
import com.bookkeepersmc.notebook.impl.networking.NotebookCustomPayloadPacketCodec;

@Mixin(targets = "net/minecraft/network/packet/payload/CustomPayload$C_idfcqkqn")
public abstract class CustomPayloadPacketCodecMixin<B extends PacketByteBuf> implements PacketCodec<B, CustomPayload>, NotebookCustomPayloadPacketCodec<B> {
	@Unique
	private CustomPayloadTypeProvider<B> customPayloadTypeProvider;

	@Override
	public void notebook_setPacketCodecProvider(CustomPayloadTypeProvider<B> customPayloadTypeProvider) {
		if (this.customPayloadTypeProvider != null) {
			throw new IllegalStateException("Payload codec provider is already set!");
		}

		this.customPayloadTypeProvider = customPayloadTypeProvider;
	}

	@WrapOperation(method = {
			"write(Lnet/minecraft/network/PacketByteBuf;Lnet/minecraft/network/packet/payload/CustomPayload$Id;Lnet/minecraft/network/packet/payload/CustomPayload;)V",
			"decode(Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/payload/CustomPayload;"
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/payload/CustomPayload$C_idfcqkqn;getPacketCodec(Lnet/minecraft/util/Identifier;)Lnet/minecraft/network/codec/PacketCodec;"))
	private PacketCodec<B, ? extends CustomPayload> wrapGetCodec(@Coerce PacketCodec<B, CustomPayload> instance, Identifier identifier, Operation<PacketCodec<B, CustomPayload>> original, B packetByteBuf) {
		if (customPayloadTypeProvider != null) {
			CustomPayload.Type<B, ? extends CustomPayload> payloadType = customPayloadTypeProvider.get(packetByteBuf, identifier);

			if (payloadType != null) {
				return payloadType.codec();
			}
		}

		return original.call(instance, identifier);
	}
}

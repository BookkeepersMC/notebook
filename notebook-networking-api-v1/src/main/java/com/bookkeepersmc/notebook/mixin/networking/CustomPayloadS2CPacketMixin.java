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

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

import com.bookkeepersmc.notebook.impl.networking.NotebookCustomPayloadPacketCodec;
import com.bookkeepersmc.notebook.impl.networking.PayloadTypeRegistryImpl;

@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin {
	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/payload/CustomPayload;create(Lnet/minecraft/network/packet/payload/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;",
					ordinal = 0
			)
	)
	private static PacketCodec<RegistryByteBuf, CustomPayload> wrapPlayCodec(CustomPayload.CodecFactory<RegistryByteBuf> unknownCodecFactory, List<CustomPayload.Type<RegistryByteBuf, ?>> types, Operation<PacketCodec<RegistryByteBuf, CustomPayload>> original) {
		PacketCodec<RegistryByteBuf, CustomPayload> codec = original.call(unknownCodecFactory, types);
		NotebookCustomPayloadPacketCodec<RegistryByteBuf> notebookCodec = (NotebookCustomPayloadPacketCodec<RegistryByteBuf>) codec;
		notebookCodec.notebook_setPacketCodecProvider((packetByteBuf, identifier) -> PayloadTypeRegistryImpl.PLAY_S2C.get(identifier));
		return codec;
	}

	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/payload/CustomPayload;create(Lnet/minecraft/network/packet/payload/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;",
					ordinal = 1
			)
	)
	private static PacketCodec<PacketByteBuf, CustomPayload> wrapConfigCodec(CustomPayload.CodecFactory<PacketByteBuf> unknownCodecFactory, List<CustomPayload.Type<PacketByteBuf, ?>> types, Operation<PacketCodec<PacketByteBuf, CustomPayload>> original) {
		PacketCodec<PacketByteBuf, CustomPayload> codec = original.call(unknownCodecFactory, types);
		NotebookCustomPayloadPacketCodec<PacketByteBuf> notebookCodec = (NotebookCustomPayloadPacketCodec<PacketByteBuf>) codec;
		notebookCodec.notebook_setPacketCodecProvider((packetByteBuf, identifier) -> PayloadTypeRegistryImpl.CONFIGURATION_S2C.get(identifier));
		return codec;
	}
}

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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.impl.networking.CustomPayloadTypeProvider;
import com.bookkeepersmc.notebook.impl.networking.NotebookCustomPayloadPacketCodec;

@Mixin(targets = "net/minecraft/network/protocol/common/custom/CustomPacketPayload$1")
public abstract class CustomPayloadPacketCodecMixin<B extends FriendlyByteBuf> implements StreamCodec<B, CustomPacketPayload>, NotebookCustomPayloadPacketCodec<B> {
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
			"writeCap",
			"decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;"
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$1;findCodec(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/codec/StreamCodec;"))
	private StreamCodec<B, ? extends CustomPacketPayload> wrapGetCodec(@Coerce StreamCodec<B, CustomPacketPayload> instance, ResourceLocation identifier, Operation<StreamCodec<B, CustomPacketPayload>> original, B packetByteBuf) {
		if (customPayloadTypeProvider != null) {
			CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload> payloadType = customPayloadTypeProvider.get(packetByteBuf, identifier);

			if (payloadType != null) {
				return payloadType.codec();
			}
		}

		return original.call(instance, identifier);
	}
}

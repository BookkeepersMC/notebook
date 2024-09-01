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
package com.bookkeepersmc.notebook.mixin.recipe.ingredient;

import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.PacketEncoder;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.recipe.CustomIngredientSync;
import com.bookkeepersmc.notebook.impl.recipe.SupportedIngredientsPacketEncoder;

@Mixin(PacketEncoder.class)
public class PacketEncoderMixin implements SupportedIngredientsPacketEncoder {
	@Unique
	private Set<Identifier> notebook_supportedCustomIngredients = Set.of();

	@Override
	public void notebook_setSupportedCustomIngredients(Set<Identifier> supportedCustomIngredients) {
		notebook_supportedCustomIngredients = supportedCustomIngredients;
	}

	@Inject(
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;Lio/netty/buffer/ByteBuf;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"
			)
	)
	private void captureEncoder(ChannelHandlerContext context, Packet<?> packet, ByteBuf buf, CallbackInfo info) {
		CustomIngredientSync.CURRENT_SUPPORTED_INGREDIENTS.set(notebook_supportedCustomIngredients);
	}

	@Inject(
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;Lio/netty/buffer/ByteBuf;)V",
			at = {
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V",
							shift = At.Shift.AFTER,
							by = 1
					),

					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/network/packet/Packet;isWritingErrorSkippable()Z"
					)
			}
	)
	private void releaseEncoder(ChannelHandlerContext context, Packet<?> packet, ByteBuf buf, CallbackInfo info) {
		CustomIngredientSync.CURRENT_SUPPORTED_INGREDIENTS.set(null);
	}
}

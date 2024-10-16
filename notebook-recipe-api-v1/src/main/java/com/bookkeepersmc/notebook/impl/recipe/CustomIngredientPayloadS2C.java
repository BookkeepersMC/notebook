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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.payload.CustomPayload;

public record CustomIngredientPayloadS2C(int protocolVersion) implements CustomPayload {
	public static final PacketCodec<PacketByteBuf, CustomIngredientPayloadS2C> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, CustomIngredientPayloadS2C::protocolVersion,
			CustomIngredientPayloadS2C::new
	);
	public static final CustomPayload.Id<CustomIngredientPayloadS2C> TYPE = new Id<>(CustomIngredientSync.PACKET_ID);

	@Override
	public Id<? extends CustomIngredientPayloadS2C> getId() {
		return TYPE;
	}
}

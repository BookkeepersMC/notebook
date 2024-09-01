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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

public record CustomIngredientPayloadC2S(int protocolVersion, Set<Identifier> registeredSerializers) implements CustomPayload {
	public static final PacketCodec<PacketByteBuf, CustomIngredientPayloadC2S> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, CustomIngredientPayloadC2S::protocolVersion,
			PacketCodecs.factory(HashSet::new, Identifier.PACKET_CODEC), CustomIngredientPayloadC2S::registeredSerializers,
			CustomIngredientPayloadC2S::new
	);
	public static final CustomPayload.Id<CustomIngredientPayloadC2S> TYPE = new Id<>(CustomIngredientSync.PACKET_ID);

	@Override
	public Id<? extends CustomPayload> getId() {
		return TYPE;
	}
}

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
package com.bookkeepersmc.notebook.impl.networking;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CommonRegisterPayload(int version, String phase, Set<ResourceLocation> channels) implements CustomPacketPayload {
	public static final Type<CommonRegisterPayload> ID = new Type<>(ResourceLocation.parse("c:register"));
	public static final StreamCodec<FriendlyByteBuf, CommonRegisterPayload> CODEC = CustomPacketPayload.codec(CommonRegisterPayload::write, CommonRegisterPayload::new);

	public static final String PLAY_PHASE = "play";
	public static final String CONFIGURATION_PHASE = "configuration";

	private CommonRegisterPayload(FriendlyByteBuf buf) {
		this(
				buf.readVarInt(),
				buf.readUtf(),
				buf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation)
		);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(version);
		buf.writeUtf(phase);
		buf.writeCollection(channels, FriendlyByteBuf::writeResourceLocation);
	}

	@Override
	public Type<CommonRegisterPayload> type() {
		return ID;
	}
}

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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.netty.util.AsciiString;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public record RegistrationPayload(Id<RegistrationPayload> id, List<Identifier> channels) implements CustomPayload {
	public static final CustomPayload.Id<RegistrationPayload> REGISTER = new CustomPayload.Id<>(NetworkingImpl.REGISTER_CHANNEL);
	public static final CustomPayload.Id<RegistrationPayload> UNREGISTER = new CustomPayload.Id<>(NetworkingImpl.UNREGISTER_CHANNEL);
	public static final PacketCodec<PacketByteBuf, RegistrationPayload> REGISTER_CODEC = codec(REGISTER);
	public static final PacketCodec<PacketByteBuf, RegistrationPayload> UNREGISTER_CODEC = codec(UNREGISTER);

	private RegistrationPayload(Id<RegistrationPayload> id, PacketByteBuf buf) {
		this(id, read(buf));
	}

	private void write(PacketByteBuf buf) {
		boolean first = true;

		for (Identifier channel : channels) {
			if (first) {
				first = false;
			} else {
				buf.writeByte(0);
			}

			buf.writeBytes(channel.toString().getBytes(StandardCharsets.US_ASCII));
		}
	}

	private static List<Identifier> read(PacketByteBuf buf) {
		List<Identifier> ids = new ArrayList<>();
		StringBuilder active = new StringBuilder();

		while (buf.isReadable()) {
			byte b = buf.readByte();

			if (b != 0) {
				active.append(AsciiString.b2c(b));
			} else {
				addId(ids, active);
				active = new StringBuilder();
			}
		}

		addId(ids, active);

		return Collections.unmodifiableList(ids);
	}

	private static void addId(List<Identifier> ids, StringBuilder sb) {
		String literal = sb.toString();

		try {
			ids.add(Identifier.parse(literal));
		} catch (InvalidIdentifierException ex) {
			NetworkingImpl.LOGGER.warn("Received invalid channel identifier \"{}\"", literal);
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return id;
	}

	private static PacketCodec<PacketByteBuf, RegistrationPayload> codec(Id<RegistrationPayload> id) {
		return CustomPayload.create(RegistrationPayload::write, buf -> new RegistrationPayload(id, buf));
	}
}

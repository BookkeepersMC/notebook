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
package com.bookkeepersmc.notebook.impl.networking.payload;

import net.minecraft.network.PacketByteBuf;

import com.bookkeepersmc.notebook.api.networking.v1.ByteBufUtils;

public class PayloadHelper {
	public static void write(PacketByteBuf byteBuf, PacketByteBuf data) {
		byteBuf.writeBytes(data.copy());
	}

	public static PacketByteBuf read(PacketByteBuf byteBuf, int maxSize) {
		assertSize(byteBuf, maxSize);

		PacketByteBuf newBuf = ByteBufUtils.create();
		newBuf.writeBytes(byteBuf.copy());
		byteBuf.skipBytes(byteBuf.readableBytes());
		return newBuf;
	}

	private static void assertSize(PacketByteBuf buf, int maxSize) {
		int size = buf.readableBytes();

		if (size < 0 || size > maxSize) {
			throw new IllegalArgumentException("Payload may not be larger than " + maxSize + " bytes");
		}
	}
}

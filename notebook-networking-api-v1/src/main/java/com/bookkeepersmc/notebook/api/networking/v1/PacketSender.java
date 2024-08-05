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
package com.bookkeepersmc.notebook.api.networking.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Represents something that supports sending packets to channels.
 * Any packets sent must be {@linkplain PayloadTypeRegistry registered} in the appropriate registry.
 */
@ApiStatus.NonExtendable
public interface PacketSender {
	/**
	 * Creates a packet from a packet payload.
	 *
	 * @param payload the packet payload
	 */
	Packet<?> createPacket(CustomPacketPayload payload);

	/**
	 * Sends a packet.
	 *
	 * @param packet the packet
	 */
	default void sendPacket(Packet<?> packet) {
		sendPacket(packet, null);
	}

	/**
	 * Sends a packet.
	 * @param payload the payload
	 */
	default void sendPacket(CustomPacketPayload payload) {
		sendPacket(createPacket(payload));
	}

	/**
	 * Sends a packet.
	 *
	 * @param packet the packet
	 * @param callback an optional callback to execute after the packet is sent, may be {@code null}.
	 */
	void sendPacket(Packet<?> packet, @Nullable PacketSendListener callback);

	/**
	 * Sends a packet.
	 *
	 * @param payload the payload
	 * @param callback an optional callback to execute after the packet is sent, may be {@code null}.
	 */
	default void sendPacket(CustomPacketPayload payload, @Nullable PacketSendListener callback) {
		sendPacket(createPacket(payload), callback);
	}

	/**
	 * Disconnects the player.
	 * @param disconnectReason the reason for disconnection
	 */
	void disconnect(Component disconnectReason);
}

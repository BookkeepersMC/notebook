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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.payload.CustomQueryPayload;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.networking.payload.PacketByteBufLoginQueryRequestPayload;
import com.bookkeepersmc.notebook.impl.networking.payload.PayloadHelper;

@Mixin(LoginQueryRequestS2CPacket.class)
public class LoginQueryRequestS2CPacketMixin {
	@Shadow
	@Final
	private static int MAX_PAYLOAD_SIZE;

	@Inject(method = "readPayload", at = @At("HEAD"), cancellable = true)
	private static void readPayload(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<CustomQueryPayload> cir) {
		cir.setReturnValue(new PacketByteBufLoginQueryRequestPayload(id, PayloadHelper.read(buf, MAX_PAYLOAD_SIZE)));
	}
}

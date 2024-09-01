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
package com.bookkeepersmc.notebook.mixin.networking.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.AbstractClientNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.configuration.FinishConfigurationS2CPacket;

import com.bookkeepersmc.notebook.impl.networking.NetworkHandlerExtensions;
import com.bookkeepersmc.notebook.impl.networking.client.ClientConfigurationNetworkAddon;
import com.bookkeepersmc.notebook.impl.networking.client.ClientNetworkingImpl;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ClientConfigurationNetworkHandler.class, priority = 999)
public abstract class ClientConfigurationNetworkHandlerMixin extends AbstractClientNetworkHandler implements NetworkHandlerExtensions {
	@Unique
	private ClientConfigurationNetworkAddon addon;

	protected ClientConfigurationNetworkHandlerMixin(Minecraft client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ClientConfigurationNetworkAddon((ClientConfigurationNetworkHandler) (Object) this, this.client);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		ClientNetworkingImpl.setClientConfigurationAddon(this.addon);
		this.addon.lateInit();
	}

	@Inject(method = "onFinishConfiguration", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/client/network/ClientConnectionState;)Lnet/minecraft/client/network/ClientPlayNetworkHandler;"))
	public void handleComplete(FinishConfigurationS2CPacket packet, CallbackInfo ci) {
		this.addon.handleComplete();
	}

	@Override
	public ClientConfigurationNetworkAddon getAddon() {
		return addon;
	}
}

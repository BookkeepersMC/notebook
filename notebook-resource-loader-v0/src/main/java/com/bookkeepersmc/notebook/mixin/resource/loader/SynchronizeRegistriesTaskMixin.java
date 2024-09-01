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
package com.bookkeepersmc.notebook.mixin.resource.loader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.configuration.SynchronizeRegistriesConfigurationTask;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.configuration.SelectKnownPacksS2CPacket;
import net.minecraft.resource.pack.KnownPack;

import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;

@Mixin(SynchronizeRegistriesConfigurationTask.class)
public abstract class SynchronizeRegistriesTaskMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("SynchronizeRegistriesTaskMixin");
	@Shadow
	@Final
	private List<KnownPack> packs;

	@Shadow
	protected abstract void method_56925(Consumer<Packet<?>> sender, Set<KnownPack> commonKnownPacks);

	@Inject(method = "method_56923", at = @At("HEAD"), cancellable = true)
	public void onSelectKnownPacks(List<KnownPack> clientKnownPacks, Consumer<Packet<?>> sender, CallbackInfo ci) {
		if (new HashSet<>(this.packs).containsAll(clientKnownPacks)) {
			this.method_56925(sender, Set.copyOf(clientKnownPacks));
			ci.cancel();
		}
	}

	@Inject(method = "method_56925", at = @At("HEAD"))
	public void method_56925(Consumer<Packet<?>> sender, Set<KnownPack> commonKnownPacks, CallbackInfo ci) {
		LOGGER.debug("Synchronizing registries with common known packs: {}", commonKnownPacks);
	}

	@Inject(method = "start", at = @At("HEAD"), cancellable = true)
	private void sendPacket(Consumer<Packet<?>> sender, CallbackInfo ci) {
		if (this.packs.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
			LOGGER.warn("Too many knownPacks: Found {}; max {}", this.packs.size(), ModResourcePackCreator.MAX_KNOWN_PACKS);
			sender.accept(new SelectKnownPacksS2CPacket(this.packs.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS)));
			ci.cancel();
		}
	}
}

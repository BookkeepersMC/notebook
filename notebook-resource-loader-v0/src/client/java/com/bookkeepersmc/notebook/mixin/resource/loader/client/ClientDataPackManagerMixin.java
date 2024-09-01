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
package com.bookkeepersmc.notebook.mixin.resource.loader.client;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resource.pack.KnownPack;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.unmapped.C_wvzzfswm;

import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;
import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackUtil;

@Mixin(C_wvzzfswm.class)
public class ClientDataPackManagerMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("ClientDataPackManagerMixin");

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/pack/VanillaDataPackProvider;createPackManager()Lnet/minecraft/resource/pack/PackManager;"))
	public PackManager createVanillaTrustedRepository() {
		return ModResourcePackUtil.createClientManager();
	}

	@ModifyReturnValue(method = "method_57049", at = @At("RETURN"))
	List<KnownPack> getCommonKnownPacksReturn(List<KnownPack> original) {
		if (original.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
			LOGGER.warn("Too many knownPacks: Found {}; max {}", original.size(), ModResourcePackCreator.MAX_KNOWN_PACKS);
			return original.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS);
		}

		return original;
	}
}

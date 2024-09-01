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

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.pack.BuiltinPackProvider;
import net.minecraft.resource.pack.PackProfile;

import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;

@Mixin(BuiltinPackProvider.class)
public class VanillaResourcePackProviderMixin {
	@Inject(method = "loadPacks", at = @At("RETURN"))
	private void addBuiltinResourcePacks(Consumer<PackProfile> consumer, CallbackInfo ci) {
		// Register mod and built-in resource packs after the vanilla built-in resource packs are registered.
		// noinspection ConstantConditions
		if ((Object) this instanceof ClientBuiltinResourcePackProvider) {
			ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.loadPacks(consumer);
		}
	}
}

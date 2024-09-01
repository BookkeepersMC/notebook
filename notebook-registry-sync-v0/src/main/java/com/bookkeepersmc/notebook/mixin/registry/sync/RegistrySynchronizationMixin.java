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
package com.bookkeepersmc.notebook.mixin.registry.sync;

import java.util.Set;
import java.util.function.BiConsumer;

import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;

import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistriesImpl;

@Mixin(DynamicRegistrySync.class)
abstract class RegistrySynchronizationMixin {

	@Dynamic("method_45961: Stream.filter in stream")
	@Inject(method = "method_56601", at = @At("HEAD"), cancellable = true)
	private static void filterNonSyncedEntries(DynamicRegistryManager.RegistryEntry<?> entry, CallbackInfoReturnable<Boolean> cir) {
		boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(entry.key());

		if (canSkip && entry.value().size() == 0) {
			cir.setReturnValue(false);
		}
	}

	@Dynamic("method_56597: Optional.isPresent in packRegistry")
	@Inject(method = "method_56596", at = @At("HEAD"), cancellable = true)
	private static void filterNonSyncedEntriesAgain(Set set, RegistryLoader.DecodingData entry, DynamicOps dynamicOps, BiConsumer biConsumer, Registry registry, CallbackInfo ci) {
		boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(registry.getKey());

		if (canSkip && registry.size() == 0) {
			ci.cancel();
		}
	}
}

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
package com.bookkeepersmc.notebook.mixin.resource.conditions;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.registry.ServerRegistryLayer;
import net.minecraft.resource.ResourceManager;


@Mixin(value = ReloadableRegistries.class, priority = 900)
public class ReloadableServerRegistriesMixin {

	@Unique
	private static final WeakHashMap<RegistryOps<?>, HolderLookup.Provider> REGISTRY_LOOKUPS = new WeakHashMap<>();

	@WrapOperation(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/HolderLookup$Provider;create(Ljava/util/stream/Stream;)Lnet/minecraft/registry/HolderLookup$Provider;"))
	private static HolderLookup.Provider storeWrapperLookup(Stream<HolderLookup.RegistryLookup<?>> lookups, Operation<HolderLookup.Provider> original, @Share("wrapper") LocalRef<HolderLookup.Provider> share) {
		HolderLookup.Provider provider = original.call(lookups);
		share.set(provider);
		return provider;
	}

	@Inject(method = "reload", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/registry/HolderLookup$Provider;createSerializationContext(Lcom/mojang/serialization/DynamicOps;)Lnet/minecraft/registry/RegistryOps;", shift = At.Shift.AFTER))
	private static void storeWrapperLookup(LayeredRegistryManager<ServerRegistryLayer> dynamicRegistries, List<Registry.TagPending<?>> pendingTagLoads, ResourceManager resourceManager, Executor prepareExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableRegistries.LoadResult>> cir, @Local RegistryOps ops, @Share("wrapper") LocalRef<HolderLookup.Provider> share) {
		REGISTRY_LOOKUPS.put(ops, share.get());
	}
}

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

import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;

import com.bookkeepersmc.notebook.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(value = ReloadableServerRegistries.class, priority = 900)
public class ReloadableServerRegistriesMixin {

	@Unique
	private static final WeakHashMap<RegistryOps<?>, HolderLookup.Provider> REGISTRY_LOOKUPS = new WeakHashMap<>();


	@WrapOperation(method = "reload", at = @At(value = "NEW", target = "net/minecraft/server/ReloadableServerRegistries$EmptyTagLookupWrapper"))
	private static ReloadableServerRegistries.EmptyTagLookupWrapper storeWrapperLookup(RegistryAccess registryManager, Operation<ReloadableServerRegistries.EmptyTagLookupWrapper> original, @Share("wrapper") LocalRef<HolderLookup.Provider> share) {
		ReloadableServerRegistries.EmptyTagLookupWrapper lookup = original.call(registryManager);
		share.set(lookup);
		return lookup;
	}

	@Inject(method = "reload", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/ReloadableServerRegistries$EmptyTagLookupWrapper;createSerializationContext(Lcom/mojang/serialization/DynamicOps;)Lnet/minecraft/resources/RegistryOps;", shift = At.Shift.AFTER))
	private static void storeWrapperLookup(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<LayeredRegistryAccess<RegistryLayer>>> cir, @Local RegistryOps ops, @Share("wrapper") LocalRef<HolderLookup.Provider> share) {
		REGISTRY_LOOKUPS.put(ops, share.get());
	}

	@Inject(method = "method_58278", at = @At("HEAD"), cancellable = true)
	private static void applyConditions(LootDataType lootDataType, RegistryOps registryOps, WritableRegistry writableRegistry, ResourceLocation resourceLocation, JsonElement jsonElement, CallbackInfo ci) {
		if (jsonElement.isJsonObject() && !ResourceConditionsImpl.applyResourceConditions(jsonElement.getAsJsonObject(), lootDataType.registryKey().location().getPath(), resourceLocation, REGISTRY_LOOKUPS.get(registryOps))) {
			ci.cancel();
		}
	}
}

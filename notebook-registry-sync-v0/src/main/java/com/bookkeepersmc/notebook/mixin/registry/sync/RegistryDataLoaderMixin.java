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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.event.registry.DynamicRegistrySetupCallback;
import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistriesImpl;
import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistryViewImpl;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
	@Unique
	private static final ThreadLocal<Boolean> IS_SERVER = ThreadLocal.withInitial(() -> false);

	@WrapOperation(
			method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/resources/RegistryDataLoader;load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;"
			)
	)
	private static RegistryAccess.Frozen wrapIsServerCall(@Coerce Object registryLoadable, RegistryAccess baseRegistryManager, List<RegistryDataLoader.RegistryData<?>> entries, Operation<RegistryAccess.Frozen> original) {
		try {
			IS_SERVER.set(true);
			return original.call(registryLoadable, baseRegistryManager, entries);
		} finally {
			IS_SERVER.set(false);
		}
	}

	@Inject(
			method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
			ordinal = 0
			)
	)
	private static void beforeLoad(@Coerce Object registryLoadable, RegistryAccess baseRegistryManager, List<RegistryDataLoader.RegistryData<?>> entries, CallbackInfoReturnable<RegistryAccess.Frozen> cir, @Local(ordinal = 1) List<RegistryDataLoader.Loader<?>> registriesList) {
		if (!IS_SERVER.get()) return;

		Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries = new IdentityHashMap<>(registriesList.size());

		for (RegistryDataLoader.Loader<?> entry : registriesList) {
			registries.put(entry.registry().key(), entry.registry());
		}

		DynamicRegistrySetupCallback.EVENT.invoker().onRegistrySetup(new DynamicRegistryViewImpl(registries));
	}

	@WrapOperation(
			method = {
					"loadContentsFromManager",
					"loadContentsFromNetwork"
			},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/registries/Registries;elementsDirPath(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/String;")
	)
	private static String prependDirectoryWithNamespace(ResourceKey<? extends Registry<?>> registryKey, Operation<String> original) {
		String originalDirectory = original.call(registryKey);
		ResourceLocation id = registryKey.location();
		if (!id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
				&& DynamicRegistriesImpl.NOTEBOOK_DYNAMIC_REGISTRY_KEYS.contains(registryKey)) {
			return id.getNamespace() + "/" + originalDirectory;
		}

		return originalDirectory;
	}
}

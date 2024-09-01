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

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.ResourceKey;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.event.registry.DynamicRegistrySetupCallback;
import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistriesImpl;
import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistryViewImpl;

@Mixin(RegistryLoader.class)
public class RegistryDataLoaderMixin {
	@Unique
	private static final ThreadLocal<Boolean> IS_SERVER = ThreadLocal.withInitial(() -> false);

	@WrapOperation(method = "method_56515", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryLoader;method_45121(Lnet/minecraft/registry/RegistryLoader$LoadingFunction;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Frozen;"))
	private static DynamicRegistryManager.Frozen wrapIsServerCall(@Coerce Object registryLoadable, List<HolderLookup.RegistryLookup<?>> baseRegistries, List<RegistryLoader.DecodingData<?>> entries, Operation<DynamicRegistryManager.Frozen> original) {
		try {
			IS_SERVER.set(true);
			return original.call(registryLoadable, baseRegistries, entries);
		} finally {
			IS_SERVER.set(false);
		}
	}

	@Inject(
			method = "method_45121",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
					ordinal = 0
			)
	)
	private static void beforeLoad(@Coerce Object registryLoadable, List<HolderLookup.RegistryLookup<?>> baseRegistries, List<RegistryLoader.DecodingData<?>> entries, CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir, @Local(ordinal = 2) List<RegistryLoader.ContentLoader<?>> registriesList) {
		if (!IS_SERVER.get()) return;

		Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries = new IdentityHashMap<>(registriesList.size());

		for (RegistryLoader.ContentLoader<?> entry : registriesList) {
			registries.put(entry.registry().getKey(), entry.registry());
		}

		DynamicRegistrySetupCallback.EVENT.invoker().onRegistrySetup(new DynamicRegistryViewImpl(registries));
	}

	@WrapOperation(
			method = {
					"loadFromManager",
					"loadFromNetwork"
			},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/registry/Registries;getDirectory(Lnet/minecraft/registry/ResourceKey;)Ljava/lang/String;"
			)
	)
	private static String prependDirectoryWithNamespace(ResourceKey<? extends Registry<?>> registryKey, Operation<String> original) {
		String originalDirectory = original.call(registryKey);
		Identifier id = registryKey.getValue();
		if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
				&& DynamicRegistriesImpl.NOTEBOOK_DYNAMIC_REGISTRY_KEYS.contains(registryKey)) {
			return id.getNamespace() + "/" + originalDirectory;
		}

		return originalDirectory;
	}
}

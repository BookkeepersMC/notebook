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
package com.bookkeepersmc.notebook.mixin.registry.sync.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import net.minecraft.client.network.ClientRegistries;
import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.ResourceKey;
import net.minecraft.resource.ResourceFactory;

import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistriesImpl;

@Mixin(ClientRegistries.class)
public class ClientRegistriesMixin {
	/**
	 * Keep the pre-24w04a behavior of removing empty registries, even if the client knows that registry.
	 */
	@WrapOperation(method = "method_62155", at = @At(value = "FIELD", target = "Lnet/minecraft/registry/RegistryLoader;SYNCED_REGISTRIES:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
	private List<RegistryLoader.DecodingData<?>> skipEmptyRegistries(Operation<List<RegistryLoader.DecodingData<?>>> operation, ResourceFactory resourceFactory, @Coerce ClientRegistriesDynamicRegistriesAccessor storage, boolean bl) {
		Map<ResourceKey<? extends Registry<?>>, List<DynamicRegistrySync.SerializedRegistryEntry>> dynamicRegistries = storage.getDynamicRegistries();

		List<RegistryLoader.DecodingData<?>> result = new ArrayList<>(operation.call());
		result.removeIf(entry -> DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(entry.registry()) && !dynamicRegistries.containsKey(entry.registry()));
		return result;
	}
}

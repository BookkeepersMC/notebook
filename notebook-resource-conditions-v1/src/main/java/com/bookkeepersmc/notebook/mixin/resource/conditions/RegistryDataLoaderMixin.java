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

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistrationInfo;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ResourceKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;

import com.bookkeepersmc.notebook.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(RegistryLoader.class)
public class RegistryDataLoaderMixin {
	@Unique
	private static final ThreadLocal<RegistryOps.RegistryInfoLookup> REGISTRIES = new ThreadLocal<>();

	@Inject(method = "loadFromManager", at = @At("HEAD"))
	private static <E> void captureRegistries(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup infoGetter, MutableRegistry<E> registry, Decoder<E> elementDecoder, Map<ResourceKey<?>, Exception> errors, CallbackInfo ci) {
		REGISTRIES.set(infoGetter);
	}

	@Inject(method = "loadFromManager", at = @At("RETURN"))
	private static <E> void releaseRegistries(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup infoGetter, MutableRegistry<E> registry, Decoder<E> elementDecoder, Map<ResourceKey<?>, Exception> errors, CallbackInfo ci) {
		REGISTRIES.remove();
	}

	@Inject(
			method = "loadElementFromResource",
			at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/gson/JsonParser;parseReader(Ljava/io/Reader;)Lcom/google/gson/JsonElement;", remap = false),
			cancellable = true
	)
	private static <E> void checkResourceCondition(
			MutableRegistry<E> registry, Decoder<E> decoder, RegistryOps<JsonElement> ops, ResourceKey<E> key, Resource resource, RegistrationInfo entryInfo,
			CallbackInfo ci, @Local Reader reader, @Local JsonElement jsonElement
	) throws IOException {
		RegistryOps.RegistryInfoLookup registryInfoLookup = REGISTRIES.get();
		if (registryInfoLookup == null) return;

		if (jsonElement.isJsonObject() && !ResourceConditionsImpl.applyResourceConditions(jsonElement.getAsJsonObject(), key.getRegistry().toString(), key.getValue(), registryInfoLookup)) {
			reader.close();
			ci.cancel();
		}
	}
}

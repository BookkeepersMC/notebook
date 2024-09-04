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

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(JsonDataLoader.class)
public class SimpleJsonResourceReloadListenerMixin {
	@Unique
	private static final Object SKIP_DATA_MARKER = new Object();

	@WrapOperation(method = "prepare(Lnet/minecraft/resource/ResourceManager;Ljava/lang/String;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
	private static DataResult<?> applyResourceConditions(Codec<?> instance, DynamicOps<JsonElement> dynamicOps, Object object, Operation<DataResult<?>> original,
														@Local(argsOnly = true) String dataType,
														@Local Map.Entry<Identifier, Resource> entry) {
		final JsonElement resourceData = (JsonElement) object;
		@Nullable RegistryOps.RegistryInfoLookup registryInfo = null;

		if (dynamicOps instanceof RegistryOpsAccessor registryOps) {
			registryInfo = registryOps.getInfoLookup();
		}

		if (resourceData.isJsonObject()) {
			JsonObject obj = resourceData.getAsJsonObject();

			if (!ResourceConditionsImpl.applyResourceConditions(obj, dataType, entry.getKey(), registryInfo)) {
				return DataResult.success(SKIP_DATA_MARKER);
			}
		}

		return original.call(instance, dynamicOps, object);
	}

	@Inject(method = "method_63568", at = @At("HEAD"), cancellable = true)
	private static void skipData(Map<?, ?> map, Identifier identifier, Object object, CallbackInfo info) {
		if (object == SKIP_DATA_MARKER) {
			info.cancel();
		}
	}
}

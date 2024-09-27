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
package com.bookkeepersmc.notebook.mixin.item;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Holder;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistrationInfo;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ResourceKey;
import net.minecraft.resource.Resource;

import com.bookkeepersmc.notebook.impl.item.EnchantmentUtil;

@Mixin(RegistryLoader.class)
abstract class RegistryLoaderMixin {
	@WrapOperation(
			method = "loadElementFromResource",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/registry/MutableRegistry;register(Lnet/minecraft/registry/ResourceKey;Ljava/lang/Object;Lnet/minecraft/registry/RegistrationInfo;)Lnet/minecraft/registry/Holder$Reference;"
			)
	)
	@SuppressWarnings("unchecked")
	private static <T> Holder.Reference<T> enchantmentKey(
			MutableRegistry<T> instance,
			ResourceKey<T> objectKey,
			Object object,
			RegistrationInfo registryEntryInfo,
			Operation<Holder.Reference<T>> original,
			MutableRegistry<T> registry,
			Decoder<T> decoder,
			RegistryOps<JsonElement> ops,
			ResourceKey<T> registryKey,
			Resource resource,
			RegistrationInfo entryInfo
	) {
		if (object instanceof Enchantment enchantment) {
			object = EnchantmentUtil.modify((ResourceKey<Enchantment>) objectKey, enchantment, EnchantmentUtil.determineSource(resource));
		}
		return original.call(instance, registryKey, object, registryEntryInfo);
	}
}

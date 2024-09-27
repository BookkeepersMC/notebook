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
package com.bookkeepersmc.notebook.impl.item;

import java.util.List;

import net.minecraft.component.DataComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.ResourceKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.pack.PackSource;

import com.bookkeepersmc.notebook.api.item.v1.EnchantmentEvents;
import com.bookkeepersmc.notebook.api.item.v1.EnchantmentSource;
import com.bookkeepersmc.notebook.impl.resource.loader.BuiltinModResourcePackSource;
import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;
import com.bookkeepersmc.notebook.impl.resource.loader.NotebookResource;
import com.bookkeepersmc.notebook.mixin.item.EnchantmentBuilderAccessor;

public class EnchantmentUtil {
	@SuppressWarnings("unchecked")
	public static Enchantment modify(ResourceKey<Enchantment> key, Enchantment originalEnchantment, EnchantmentSource source) {
		Enchantment.Builder builder = Enchantment.builder(originalEnchantment.definition());
		EnchantmentBuilderAccessor accessor = (EnchantmentBuilderAccessor) builder;
		builder.withExclusiveSet(originalEnchantment.exclusiveSet());
		accessor.getEffectMap().putAll(originalEnchantment.effects());

		originalEnchantment.effects().stream()
				.forEach(component -> {
					if (component.value() instanceof List<?> valueList) {
						// component type cast is checked by the value
						accessor.invokeGetEffectsList((DataComponentType<List<Object>>) component.type())
								.addAll(valueList);
					}
				});

		EnchantmentEvents.MODIFY.invoker().modify(key, builder, source);

		return new Enchantment(
				originalEnchantment.description(),
				accessor.getDefinition(),
				accessor.getExclusiveSet(),
				accessor.getEffectMap().build()
		);
	}

	public static EnchantmentSource determineSource(Resource resource) {
		if (resource != null) {
			PackSource packSource = ((NotebookResource) resource).getNotebookPackSource();

			if (packSource == PackSource.PACK_SOURCE_BUILTIN) {
				return EnchantmentSource.VANILLA;
			} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModResourcePackSource) {
				return EnchantmentSource.MOD;
			}
		}
		// If not builtin or mod, assume external data pack.
		// It might also be a virtual enchantment injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return EnchantmentSource.DATA_PACK;
	}

	private EnchantmentUtil() { }
}

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
package com.bookkeepersmc.notebook.mixin.client.rendering;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.util.collection.IdList;

import com.bookkeepersmc.notebook.impl.client.rendering.ColorProviderRegistryImpl;

@Mixin(ItemColors.class)
public class ItemColorsMixin implements ColorProviderRegistryImpl.ColorMapperHolder<ItemConvertible, ItemColorProvider> {
	@Shadow
	@Final
	private IdList<ItemColorProvider> providers;

	@Inject(method = "create", at = @At("RETURN"))
	private static void create(BlockColors blockMap, CallbackInfoReturnable<ItemColors> info) {
		ColorProviderRegistryImpl.ITEM.initialize(info.getReturnValue());
	}

	@Override
	public ItemColorProvider get(ItemConvertible item) {
		return providers.get(BuiltInRegistries.ITEM.getRawId(item.asItem()));
	}
}

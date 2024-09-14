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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;

import com.bookkeepersmc.notebook.api.item.v1.CustomDamageHandler;
import com.bookkeepersmc.notebook.api.item.v1.EquipmentSlotProvider;
import com.bookkeepersmc.notebook.api.item.v1.ItemExtensions;
import com.bookkeepersmc.notebook.impl.item.NotebookItemExtensions;
import com.bookkeepersmc.notebook.impl.item.NotebookItemInternals;

@Mixin(Item.class)
abstract class ItemMixin implements ItemExtensions, NotebookItemExtensions {
	@Unique
	@Nullable
	private EquipmentSlotProvider equipmentSlotProvider;

	@Unique
	@Nullable
	private CustomDamageHandler customDamageHandler;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstruct(Item.Settings settings, CallbackInfo info) {
		NotebookItemInternals.onBuild(settings, (Item) (Object) this);
	}

	@Override
	@Nullable
	public EquipmentSlotProvider notebook_getEquipmentSlotProvider() {
		return equipmentSlotProvider;
	}

	@Override
	public void notebook_setEquipmentSlotProvider(@Nullable EquipmentSlotProvider equipmentSlotProvider) {
		this.equipmentSlotProvider = equipmentSlotProvider;
	}

	@Override
	@Nullable
	public CustomDamageHandler notebook_getCustomDamageHandler() {
		return customDamageHandler;
	}

	@Override
	public void notebook_setCustomDamageHandler(@Nullable CustomDamageHandler handler) {
		this.customDamageHandler = handler;
	}
}

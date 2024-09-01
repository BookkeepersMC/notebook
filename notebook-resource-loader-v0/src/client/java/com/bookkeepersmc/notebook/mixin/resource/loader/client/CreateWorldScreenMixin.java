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
package com.bookkeepersmc.notebook.mixin.resource.loader.client;

import java.io.File;

import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.server.world.FeatureAndDataSettings;

import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;
import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackUtil;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
	@Shadow
	private PackManager packManager;

	private CreateWorldScreenMixin() {
		super(null);
	}

	@ModifyVariable(method = "open",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;createDefaultLoadConfig(Lnet/minecraft/resource/pack/PackManager;Lnet/minecraft/server/world/FeatureAndDataSettings;)Lnet/minecraft/server/WorldLoader$InitConfig;"))
	private static PackManager onCreateResManagerInit(PackManager manager) {
		// Add mod data packs to the initial res pack manager so they are active even if the user doesn't use custom data packs
		manager.providers.add(new ModResourcePackCreator(ResourceType.SERVER_DATA));
		return manager;
	}

	@Redirect(method = "open", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/FeatureAndDataSettings;MINIMAL:Lnet/minecraft/server/world/FeatureAndDataSettings;", ordinal = 0))
	private static FeatureAndDataSettings replaceDefaultSettings() {
		return ModResourcePackUtil.createDefaultDataConfiguration();
	}

	@Inject(method = "getScannedDataPack",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/pack/PackManager;scanPacks()V", shift = At.Shift.BEFORE))
	private void onScanPacks(CallbackInfoReturnable<Pair<File, PackManager>> cir) {
		// Allow to display built-in data packs in the data pack selection screen at world creation.
		this.packManager.providers.add(new ModResourcePackCreator(ResourceType.SERVER_DATA));
	}
}

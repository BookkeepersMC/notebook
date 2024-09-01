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
package com.bookkeepersmc.notebook.mixin.event.lifecycle;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.registry.ServerRegistryLayer;
import net.minecraft.server.ServerReloadableResources;
import net.minecraft.server.command.CommandManager;

import com.bookkeepersmc.notebook.api.event.lifecycle.v1.CommonLifecycleEvents;

@Mixin(ServerReloadableResources.class)
public class ReloadableServerResourcesMixin {
	@Unique
	private DynamicRegistryManager dynamicRegistryManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(LayeredRegistryManager<ServerRegistryLayer> combinedDynamicRegistries, HolderLookup.Provider wrapperLookup, FeatureFlagBitSet featureSet, CommandManager.RegistrationEnvironment registrationEnvironment, List list, int i, CallbackInfo ci) {
		dynamicRegistryManager = combinedDynamicRegistries.getCompositeManager();
	}

	@Inject(method = "method_61248", at = @At("TAIL"))
	private void hookRefresh(CallbackInfo info) {
		CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(dynamicRegistryManager, false);
	}
}

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
package com.bookkeepersmc.notebook.mixin.resource.loader;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.llamalad7.mixinextras.sugar.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.FilePackProvider;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.PackProvider;
import net.minecraft.resource.pack.PackSource;

import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;
import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackUtil;
import com.bookkeepersmc.notebook.impl.resource.loader.NotebookResourcePackProfile;

@Mixin(PackManager.class)
public abstract class ResourcePackManagerMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("ResourcePackManagerMixin");

	@Shadow
	@Final
	@Mutable
	public Set<PackProvider> providers;

	@Shadow
	private Map<String, PackProfile> profiles;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void construct(PackProvider[] resourcePackProviders, CallbackInfo info) {
		// Use a LinkedHashSet to preserve ordering
		providers = new LinkedHashSet<>(providers);

		// Search resource pack providers to find any server-related pack provider.
		boolean shouldAddServerProvider = false;

		for (PackProvider provider : this.providers) {
			if (provider instanceof FilePackProvider
					&& (((FilePackProvider) provider).source == PackSource.PACK_SOURCE_WORLD
					|| ((FilePackProvider) provider).source == PackSource.PACK_SOURCE_SERVER)) {
				shouldAddServerProvider = true;
				break;
			}
		}

		// On server, add the mod resource pack provider.
		if (shouldAddServerProvider) {
			providers.add(new ModResourcePackCreator(ResourceType.SERVER_DATA));
		}
	}

	@Inject(method = "buildEnabledProfiles", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;", shift = At.Shift.BEFORE))
	private void handleAutoEnableDisable(Collection<String> enabledNames, CallbackInfoReturnable<List<PackProfile>> cir, @Local List<PackProfile> enabledAfterFirstRun) {
		ModResourcePackUtil.refreshAutoEnabledPacks(enabledAfterFirstRun, this.profiles);
	}

	@Inject(method = "enablePackProfile", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
	private void handleAutoEnable(String profile, CallbackInfoReturnable<Boolean> cir, @Local List<PackProfile> newlyEnabled) {
		if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
			ModResourcePackUtil.refreshAutoEnabledPacks(newlyEnabled, this.profiles);
		}
	}

	@Inject(method = "disablePackProfile", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
	private void handleAutoDisable(String profile, CallbackInfoReturnable<Boolean> cir, @Local List<PackProfile> enabled) {
		if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
			Set<String> currentlyEnabled = enabled.stream().map(PackProfile::getName).collect(Collectors.toSet());
			enabled.removeIf(p -> !((NotebookResourcePackProfile) p).notebook_parentsEnabled(currentlyEnabled));
			LOGGER.debug("[Notebook] Internal pack auto-removed upon disabling {}, result: {}", profile, enabled.stream().map(PackProfile::getName).toList());
		}
	}
}

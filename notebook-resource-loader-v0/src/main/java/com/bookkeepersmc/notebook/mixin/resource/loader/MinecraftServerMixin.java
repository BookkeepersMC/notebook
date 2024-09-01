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

import java.net.Proxy;
import java.util.List;

import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.pack.KnownPack;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.WorldStem;
import net.minecraft.world.storage.WorldSaveStorage;

import com.bookkeepersmc.notebook.impl.resource.loader.BuiltinModResourcePackSource;
import com.bookkeepersmc.notebook.impl.resource.loader.ModNioResourcePack;
import com.bookkeepersmc.notebook.impl.resource.loader.NotebookOriginalKnownPacksGetter;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements NotebookOriginalKnownPacksGetter {
	@Unique
	private List<KnownPack> notebook_originalKnownPacks;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(Thread serverThread, WorldSaveStorage.Session session, PackManager dataPackManager, WorldStem saveLoader, Proxy proxy, DataFixer dataFixer, Services apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
		this.notebook_originalKnownPacks = saveLoader.resourceManager().streamResourcePacks().flatMap(pack -> pack.getLocationInfo().knownPackInfo().stream()).toList();
	}

	@Redirect(method = "loadDataPacks", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
	private static boolean onCheckDisabled(List<String> list, Object o, PackManager resourcePackManager) {
		String profileId = (String) o;
		boolean contains = list.contains(profileId);

		if (contains) {
			return true;
		}

		PackProfile profile = resourcePackManager.getProfile(profileId);

		if (profile.getSource() instanceof BuiltinModResourcePackSource) {
			try (ResourcePack pack = profile.createPack()) {
				// Prevents automatic load for built-in data packs provided by mods.
				return pack instanceof ModNioResourcePack modPack && !modPack.getActivationType().isEnabledByDefault();
			}
		}

		return false;
	}

	@Override
	public List<KnownPack> notebook_getOriginalKnownPacks() {
		return this.notebook_originalKnownPacks;
	}
}

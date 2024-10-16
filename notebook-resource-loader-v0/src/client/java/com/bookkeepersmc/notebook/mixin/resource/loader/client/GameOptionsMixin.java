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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.option.GameOptions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.ResourcePack;

import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.notebook.impl.resource.loader.ModNioResourcePack;
import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	@Shadow
	public List<String> resourcePacks;

	@Shadow
	@Final
	static Logger LOGGER;

	@Inject(method = "load", at = @At("RETURN"))
	private void onLoad(CallbackInfo ci) {
		// Track built-in resource packs if they are enabled by default.
		// - If there is NO value with matching resource pack id, add it to the enabled packs and the tracker file.
		// - If there is a matching value and pack id, do not add it to the enabled packs and let
		//   the options value decides if it is enabled or not.
		// - If there is a value without matching pack id (e.g. because the mod is removed),
		//   remove it from the tracker file so that it would be enabled again if added back later.

		Path dataDir = NotebookLoader.getInstance().getGameDir().resolve("data");

		if (Files.notExists(dataDir)) {
			try {
				Files.createDirectories(dataDir);
			} catch (IOException e) {
				LOGGER.warn("[Resource Loader] Could not create data directory: " + dataDir.toAbsolutePath());
			}
		}

		Path trackerFile = dataDir.resolve("notebookDefaultResourcePacks.dat");
		Set<String> trackedPacks = new HashSet<>();

		if (Files.exists(trackerFile)) {
			try {
				NbtCompound data = NbtIo.readCompressed(trackerFile, NbtTagSizeTracker.createWithUnlimitedBytes());
				NbtList values = data.getList("values", NbtElement.STRING_TYPE);

				for (int i = 0; i < values.size(); i++) {
					trackedPacks.add(values.getString(i));
				}
			} catch (IOException e) {
				LOGGER.warn("[Resource Loader] Could not read " + trackerFile.toAbsolutePath(), e);
			}
		}

		Set<String> removedPacks = new HashSet<>(trackedPacks);
		Set<String> resourcePacks = new LinkedHashSet<>(this.resourcePacks);

		List<PackProfile> profiles = new ArrayList<>();
		ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.loadPacks(profiles::add);

		for (PackProfile profile : profiles) {
			// Always add "Notebook Mods" pack to enabled resource packs.
			if (profile.getName().equals(ModResourcePackCreator.NOTEBOOK)) {
				resourcePacks.add(profile.getName());
				continue;
			}

			try (ResourcePack pack = profile.createPack()) {
				if (pack instanceof ModNioResourcePack builtinPack && builtinPack.getActivationType().isEnabledByDefault()) {
					if (trackedPacks.add(builtinPack.getName())) {
						resourcePacks.add(profile.getName());
					} else {
						removedPacks.remove(builtinPack.getName());
					}
				}
			}
		}

		try {
			NbtList values = new NbtList();

			for (String id : trackedPacks) {
				if (!removedPacks.contains(id)) {
					values.add(NbtString.of(id));
				}
			}

			NbtCompound nbt = new NbtCompound();
			nbt.put("values", values);
			NbtIo.writeCompressed(nbt, trackerFile);
		} catch (IOException e) {
			LOGGER.warn("[Resource Loader] Could not write to " + trackerFile.toAbsolutePath(), e);
		}

		this.resourcePacks = new ArrayList<>(resourcePacks);
	}
}

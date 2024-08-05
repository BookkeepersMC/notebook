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
package com.bookkeepersmc.notebook.impl.resource.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import com.bookkeepersmc.notebook.api.resource.ModResourcePack;

/**
 * Represents a resource pack provider for mods and built-in mods resource packs.
 */
public class ModResourcePackCreator implements RepositorySource {
	/**
	 * The ID of the root resource pack profile for bundled packs.
	 */
	public static final String NOTEBOOK = "notebook";
	private static final String PROGRAMMER_ART = "programmer_art";
	private static final String HIGH_CONTRAST = "high_contrast";
	public static final Set<String> POST_CHANGE_HANDLE_REQUIRED = Set.of(NOTEBOOK, PROGRAMMER_ART, HIGH_CONTRAST);
	@VisibleForTesting
	public static final Predicate<Set<String>> BASE_PARENT = enabled -> enabled.contains(NOTEBOOK);
	@VisibleForTesting
	public static final Predicate<Set<String>> PROGRAMMER_ART_PARENT = enabled -> enabled.contains(NOTEBOOK) && enabled.contains(PROGRAMMER_ART);
	@VisibleForTesting
	public static final Predicate<Set<String>> HIGH_CONTRAST_PARENT = enabled -> enabled.contains(NOTEBOOK) && enabled.contains(HIGH_CONTRAST);
	/**
	 * This can be used to check if a pack profile is for mod-provided packs.
	 */
	public static final PackSource RESOURCE_PACK_SOURCE = new PackSource() {
		@Override
		public Component decorate(Component packName) {
			return Component.translatable("pack.nameAndSource", packName, Component.translatable("pack.source.notebookmod"));
		}

		@Override
		public boolean shouldAddAutomatically() {
			return true;
		}
	};
	public static final ModResourcePackCreator CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackCreator(PackType.CLIENT_RESOURCES);
	/**
	 * The maximum number of known data packs requested from the client, including vanilla data packs.
	 */
	public static final int MAX_KNOWN_PACKS = Integer.getInteger("notebook-resource-loader-v0:maxKnownPacks", 1024);

	private final PackType type;
	private final PackSelectionConfig activationInfo;
	private final boolean forClientDataPackManager;

	public ModResourcePackCreator(PackType type) {
		this(type, false);
	}

	protected ModResourcePackCreator(PackType type, boolean forClientDataPackManager) {
		this.type = type;
		this.activationInfo = new PackSelectionConfig(!forClientDataPackManager, Pack.Position.TOP, false);
		this.forClientDataPackManager = forClientDataPackManager;
	}

	/**
	 * Registers the resource packs.
	 *
	 * @param consumer The resource pack profile consumer.
	 */
	@Override
	public void loadPacks(Consumer<Pack> consumer) {
		/*
			Register order rule in this provider:
			1. Mod resource packs
			2. Mod built-in resource packs

			Register order rule globally:
			1. Default and Vanilla built-in resource packs
			2. Mod resource packs
			3. Mod built-in resource packs
			4. User resource packs
		 */

		PackLocationInfo metadata = new PackLocationInfo(
				NOTEBOOK,
				Component.translatable("pack.name.notebookMods"),
				RESOURCE_PACK_SOURCE,
				Optional.empty()
		);

		consumer.accept(Pack.readMetaAndCreate(
				metadata,
				new PlaceholderResourcePack.Factory(this.type, metadata),
				this.type,
				this.activationInfo
		));

		// Build a list of mod resource packs.
		registerModPack(consumer, null, BASE_PARENT);

		if (this.type == PackType.CLIENT_RESOURCES) {
			// Programmer Art/High Contrast data packs can never be enabled.
			registerModPack(consumer, PROGRAMMER_ART, PROGRAMMER_ART_PARENT);
			registerModPack(consumer, HIGH_CONTRAST, HIGH_CONTRAST_PARENT);
		}

		// Register all built-in resource packs provided by mods.
		ResourceManagerHelperImpl.registerBuiltinResourcePacks(this.type, consumer);
	}

	private void registerModPack(Consumer<Pack> consumer, @Nullable String subPath, Predicate<Set<String>> parents) {
		List<ModResourcePack> packs = new ArrayList<>();
		ModResourcePackUtil.appendModResourcePacks(packs, this.type, subPath);

		for (ModResourcePack pack : packs) {
			Pack profile = Pack.readMetaAndCreate(
					pack.location(),
					new ModResourcePackFactory(pack),
					this.type,
					this.activationInfo
			);

			if (profile != null) {
				if (!forClientDataPackManager) {
					((NotebookResourcePackProfile) profile).notebook_setParentsPredicate(parents);
				}

				consumer.accept(profile);
			}
		}
	}
}

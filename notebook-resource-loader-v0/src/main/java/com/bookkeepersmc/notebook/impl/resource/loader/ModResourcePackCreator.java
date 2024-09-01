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

import net.minecraft.resource.PackPosition;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackLocationInfo;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.PackProvider;
import net.minecraft.resource.pack.PackSource;
import net.minecraft.text.Text;

import com.bookkeepersmc.notebook.api.resource.ModResourcePack;

/**
 * Represents a resource pack provider for mods and built-in mods resource packs.
 */
public class ModResourcePackCreator implements PackProvider {
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
		public Text decorate(Text packName) {
			return Text.translatable("pack.nameAndSource", packName, Text.translatable("pack.source.notebookmod"));
		}

		@Override
		public boolean shouldAddAutomatically() {
			return true;
		}
	};
	public static final ModResourcePackCreator CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackCreator(ResourceType.CLIENT_RESOURCES);
	/**
	 * The maximum number of known data packs requested from the client, including vanilla data packs.
	 */
	public static final int MAX_KNOWN_PACKS = Integer.getInteger("notebook-resource-loader-v0:maxKnownPacks", 1024);

	private final ResourceType type;
	private final PackPosition activationInfo;
	private final boolean forClientDataPackManager;

	public ModResourcePackCreator(ResourceType type) {
		this(type, false);
	}

	protected ModResourcePackCreator(ResourceType type, boolean forClientDataPackManager) {
		this.type = type;
		this.activationInfo = new PackPosition(!forClientDataPackManager, PackProfile.InsertionPosition.TOP, false);
		this.forClientDataPackManager = forClientDataPackManager;
	}

	/**
	 * Registers the resource packs.
	 *
	 * @param consumer The resource pack profile consumer.
	 */
	@Override
	public void loadPacks(Consumer<PackProfile> consumer) {
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
				Text.translatable("pack.name.notebookMods"),
				RESOURCE_PACK_SOURCE,
				Optional.empty()
		);

		consumer.accept(PackProfile.of(
				metadata,
				new PlaceholderResourcePack.Factory(this.type, metadata),
				this.type,
				this.activationInfo
		));

		// Build a list of mod resource packs.
		registerModPack(consumer, null, BASE_PARENT);

		if (this.type == ResourceType.CLIENT_RESOURCES) {
			// Programmer Art/High Contrast data packs can never be enabled.
			registerModPack(consumer, PROGRAMMER_ART, PROGRAMMER_ART_PARENT);
			registerModPack(consumer, HIGH_CONTRAST, HIGH_CONTRAST_PARENT);
		}

		// Register all built-in resource packs provided by mods.
		ResourceManagerHelperImpl.registerBuiltinResourcePacks(this.type, consumer);
	}

	private void registerModPack(Consumer<PackProfile> consumer, @Nullable String subPath, Predicate<Set<String>> parents) {
		List<ModResourcePack> packs = new ArrayList<>();
		ModResourcePackUtil.appendModResourcePacks(packs, this.type, subPath);

		for (ModResourcePack pack : packs) {
			PackProfile profile = PackProfile.of(
					pack.getLocationInfo(),
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

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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackLocationInfo;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.BuiltinMetadata;
import net.minecraft.resource.pack.metadata.PackResourceMetadataSection;
import net.minecraft.resource.pack.metadata.ResourceMetadataSectionReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record PlaceholderResourcePack(ResourceType type, PackLocationInfo metadata) implements ResourcePack {
	private static final Text DESCRIPTION_TEXT = Text.translatable("pack.description.modResources");

	public PackResourceMetadataSection getMetadata() {
		return ModResourcePackUtil.getMetadataPack(
				SharedConstants.getGameVersion().getResourceVersion(type),
				DESCRIPTION_TEXT
		);
	}

	@Nullable
	@Override
	public ResourceIoSupplier<InputStream> openRoot(String... segments) {
		if (segments.length > 0) {
			switch (segments[0]) {
			case "pack.mcmeta":
				return () -> {
					String metadata = ModResourcePackUtil.GSON.toJson(PackResourceMetadataSection.TYPE.toJson(getMetadata()));
					return IOUtils.toInputStream(metadata, StandardCharsets.UTF_8);
				};
			case "pack.png":
				return ModResourcePackUtil::getDefaultIcon;
			}
		}

		return null;
	}

	/**
	 * This pack has no actual contents.
	 */
	@Nullable
	@Override
	public ResourceIoSupplier<InputStream> open(ResourceType type, Identifier id) {
		return null;
	}

	@Override
	public void listResources(ResourceType type, String namespace, String prefix, ResourceConsumer consumer) {
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return Collections.emptySet();
	}

	@Nullable
	@Override
	public <T> T parseMetadata(ResourceMetadataSectionReader<T> metaReader) {
		return BuiltinMetadata.of(PackResourceMetadataSection.TYPE, getMetadata()).get(metaReader);
	}

	@Override
	public PackLocationInfo getLocationInfo() {
		return metadata;
	}

	@Override
	public String getName() {
		return ModResourcePackCreator.NOTEBOOK;
	}

	@Override
	public void close() {
	}

	public record Factory(ResourceType type, PackLocationInfo metadata) implements PackProfile.PackFactory {
		@Override
		public ResourcePack openPrimary(PackLocationInfo var1) {
			return new PlaceholderResourcePack(this.type, metadata);
		}

		@Override
		public ResourcePack open(PackLocationInfo var1, PackProfile.Metadata metadata) {
			return openPrimary(var1);
		}
	}
}

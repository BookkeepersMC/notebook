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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;

public record PlaceholderResourcePack(PackType type, PackLocationInfo metadata) implements PackResources {
	private static final Component DESCRIPTION_TEXT = Component.translatable("pack.description.modResources");

	public PackMetadataSection getMetadata() {
		return ModResourcePackUtil.getMetadataPack(
				SharedConstants.getCurrentVersion().getPackVersion(type),
				DESCRIPTION_TEXT
		);
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... segments) {
		if (segments.length > 0) {
			switch (segments[0]) {
			case "pack.mcmeta":
				return () -> {
					String metadata = ModResourcePackUtil.GSON.toJson(PackMetadataSection.TYPE.toJson(getMetadata()));
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
	public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
		return null;
	}

	@Override
	public void listResources(PackType type, String namespace, String prefix, ResourceOutput consumer) {
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return Collections.emptySet();
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
		return BuiltInMetadata.of(PackMetadataSection.TYPE, getMetadata()).get(metaReader);
	}

	@Override
	public PackLocationInfo location() {
		return metadata;
	}

	@Override
	public String packId() {
		return ModResourcePackCreator.NOTEBOOK;
	}

	@Override
	public void close() {
	}

	public record Factory(PackType type, PackLocationInfo metadata) implements Pack.ResourcesSupplier {
		@Override
		public PackResources openPrimary(PackLocationInfo var1) {
			return new PlaceholderResourcePack(this.type, metadata);
		}

		@Override
		public PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
			return openPrimary(var1);
		}
	}
}

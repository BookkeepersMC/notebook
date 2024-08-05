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

import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import com.bookkeepersmc.notebook.api.resource.ModResourcePack;

public record ModResourcePackFactory(ModResourcePack pack) implements Pack.ResourcesSupplier {
	@Override
	public PackResources openPrimary(PackLocationInfo var1) {
		return pack;
	}

	@Override
	public PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
		if (metadata.overlays().isEmpty()) {
			return pack;
		} else {
			List<PackResources> overlays = new ArrayList<>(metadata.overlays().size());

			for (String overlay : metadata.overlays()) {
				overlays.add(pack.createOverlay(overlay));
			}

			return new CompositePackResources(pack, overlays);
		}
	}
}

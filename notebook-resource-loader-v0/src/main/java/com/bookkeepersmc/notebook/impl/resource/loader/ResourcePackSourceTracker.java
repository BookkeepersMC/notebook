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

import java.util.WeakHashMap;

import net.minecraft.resource.pack.PackSource;
import net.minecraft.resource.pack.ResourcePack;

/**
 * Tracks the sources of resource packs in a global weak hash map.
 * {@link PackResources} doesn't hold a reference to its {@link PackSource}
 * so we store the source in the map when the resource packs are created.
 * See {@link com.bookkeepersmc.notebook.mixin.resource.loader.ResourcePackProfileMixin ResourcePackProfileMixin}.
 *
 * <p>The sources are later read for use in {@link NotebookResource} and {@link NotebookResourceImpl}.
 */
public final class ResourcePackSourceTracker {
	// Use a weak hash map so that if resource packs would be deleted, this won't keep them alive.
	private static final WeakHashMap<ResourcePack, PackSource> SOURCES = new WeakHashMap<>();

	/**
	 * Gets the source of a pack.
	 *
	 * @param pack the resource pack
	 * @return the source, or {@link PackSource#DEFAULT} if not tracked
	 */
	public static PackSource getSource(ResourcePack pack) {
		return SOURCES.getOrDefault(pack, PackSource.PACK_SOURCE_NONE);
	}

	/**
	 * Sets the source of a pack.
	 *
	 * @param pack the resource pack
	 * @param source the source
	 */
	public static void setSource(ResourcePack pack, PackSource source) {
		SOURCES.put(pack, source);
	}
}

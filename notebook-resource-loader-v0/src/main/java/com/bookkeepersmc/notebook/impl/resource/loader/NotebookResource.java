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

import org.slf4j.LoggerFactory;

import net.minecraft.resource.pack.PackSource;

/**
 * Extensions to {@link net.minecraft.server.packs.resources.Resource}.
 * Automatically implemented there via a mixin.
 * Currently, this is only for use in other API modules.
 */
public interface NotebookResource {
	/**
	 * Gets the resource pack source of this resource.
	 * The source is used to separate vanilla/mod resources from user resources in Notebook API.
	 *
	 * <p>Custom {@link net.minecraft.server.packs.resources.Resource} implementations should override this method.
	 *
	 * @return the resource pack source
	 */
	default PackSource getNotebookPackSource() {
		LoggerFactory.getLogger(NotebookResource.class).error("Unknown Resource implementation {}, returning PACK_SOURCE_NONE as the source", getClass().getName());
		return PackSource.PACK_SOURCE_NONE;
	}
}

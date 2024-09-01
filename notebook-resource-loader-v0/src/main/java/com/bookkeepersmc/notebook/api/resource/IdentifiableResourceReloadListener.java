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
package com.bookkeepersmc.notebook.api.resource;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

/**
 * Interface for "identifiable" resource reload listeners.
 *
 * <p>"Identifiable" listeners have a unique identifier, which can be depended on,
 * and can provide dependencies that they would like to see executed before
 * themselves.
 *
 * @see ResourceReloadListenerKeys
 */
public interface IdentifiableResourceReloadListener extends ResourceReloader {
	/**
	 * @return The unique identifier of this listener.
	 */
	Identifier getNotebookId();

	/**
	 * @return The identifiers of listeners this listener expects to have been
	 * executed before itself. Please keep in mind that this only takes effect
	 * during the application stage!
	 */
	default Collection<Identifier> getNotebookDependencies() {
		return Collections.emptyList();
	}
}

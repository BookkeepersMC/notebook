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
package com.bookkeepersmc.notebook.api.event.registry;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.impl.registry.sync.ListenableRegistry;

@FunctionalInterface
public interface RegistryIdRemapCallback<T> {
	void onRemap(RemapState<T> state);

	interface RemapState<T> {
		Int2IntMap getSyncIdChangeMap();

		ResourceLocation getIdFromOld(int oldSyncId);

		ResourceLocation getIdFromNew(int newSyncId);
	}

	static <T> Event<RegistryIdRemapCallback<T>> event(Registry<T> registry) {
		return ListenableRegistry.get(registry).notebook_getRemapEvent();
	}
}

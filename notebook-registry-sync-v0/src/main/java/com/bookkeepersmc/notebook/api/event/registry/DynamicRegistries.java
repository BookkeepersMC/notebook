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

import java.util.List;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;

import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistriesImpl;

public final class DynamicRegistries {
	private DynamicRegistries() {
	}

	public static @Unmodifiable List<RegistryDataLoader.RegistryData<?>> getDynamicRegistries() {
		return DynamicRegistriesImpl.getDynamicRegistries();
	}

	public static <T> void register(ResourceKey<? extends Registry<T>> key, Codec<T> codec) {
		DynamicRegistriesImpl.register(key, codec);
	}

	public static <T> void registerSynced(ResourceKey<? extends Registry<T>> key, Codec<T> codec, SyncOption... options) {
		registerSynced(key, codec, codec, options);
	}

	public static <T> void registerSynced(ResourceKey<? extends Registry<T>> key, Codec<T> dataCodec, Codec<T> networkCodec, SyncOption... options) {
		DynamicRegistriesImpl.register(key, dataCodec);
		DynamicRegistriesImpl.addSyncedRegistry(key, networkCodec, options);
	}

	public enum SyncOption {
		SKIP_WHEN_EMPTY
	}
}

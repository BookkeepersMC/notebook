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
package com.bookkeepersmc.notebook.impl.registry.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.ResourceKey;

import com.bookkeepersmc.notebook.api.event.registry.DynamicRegistries;

public final class DynamicRegistriesImpl {
	private static final List<RegistryLoader.DecodingData<?>> DYNAMIC_REGISTRIES = new ArrayList<>(RegistryLoader.WORLDGEN_REGISTRIES);
	public static final Set<ResourceKey<?>> NOTEBOOK_DYNAMIC_REGISTRY_KEYS = new HashSet<>();
	public static final Set<ResourceKey<? extends Registry<?>>> DYNAMIC_REGISTRY_KEYS = new HashSet<>();
	public static final Set<ResourceKey<? extends Registry<?>>> SKIP_EMPTY_SYNC_REGISTRIES = new HashSet<>();

	static {
		for (RegistryLoader.DecodingData<?> loader : RegistryLoader.WORLDGEN_REGISTRIES) {
			DYNAMIC_REGISTRY_KEYS.add(loader.registry());
		}
	}

	private DynamicRegistriesImpl() {
	}

	public static @Unmodifiable List<RegistryLoader.DecodingData<?>> getDynamicRegistries() {
		return List.copyOf(DYNAMIC_REGISTRIES);
	}

	public static <T> RegistryLoader.DecodingData<T> register(ResourceKey<? extends Registry<T>> key, Codec<T> codec) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(codec, "Codec cannot be null");

		if (!DYNAMIC_REGISTRY_KEYS.add(key)) {
			throw new IllegalArgumentException("Dynamic registry " + key + " has already been registered!");
		}

		var entry = new RegistryLoader.DecodingData<>(key, codec, false);
		DYNAMIC_REGISTRIES.add(entry);
		NOTEBOOK_DYNAMIC_REGISTRY_KEYS.add(key);
		return entry;
	}

	public static <T> void addSyncedRegistry(ResourceKey<? extends Registry<T>> key, Codec<T> networkCodec, DynamicRegistries.SyncOption... options) {
		Objects.requireNonNull(key, "Registry key cannot be null");
		Objects.requireNonNull(networkCodec, "Network Codec cannot be null");
		Objects.requireNonNull(networkCodec, "Options cannot be null");

		if (!(RegistryLoader.SYNCED_REGISTRIES instanceof ArrayList<RegistryLoader.DecodingData<?>>)) {
			RegistryLoader.SYNCED_REGISTRIES = new ArrayList<>(RegistryLoader.SYNCED_REGISTRIES);
		}

		RegistryLoader.SYNCED_REGISTRIES.add(new RegistryLoader.DecodingData<>(key, networkCodec, false));

		if (!(DynamicRegistrySync.SYNCED_CODECS instanceof HashSet<ResourceKey<? extends Registry<?>>>)) {
			DynamicRegistrySync.SYNCED_CODECS = new HashSet<>(DynamicRegistrySync.SYNCED_CODECS);
		}

		DynamicRegistrySync.SYNCED_CODECS.add(key);

		for (DynamicRegistries.SyncOption option : options) {
			if (option == DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY) {
				SKIP_EMPTY_SYNC_REGISTRIES.add(key);
			}
		}
	}
}

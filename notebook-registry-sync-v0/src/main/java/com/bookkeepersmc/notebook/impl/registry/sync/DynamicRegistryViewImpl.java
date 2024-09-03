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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.ResourceKey;

import com.bookkeepersmc.notebook.api.event.registry.DynamicRegistryView;
import com.bookkeepersmc.notebook.api.event.registry.RegistryEntryAddedCallback;

public final class DynamicRegistryViewImpl implements DynamicRegistryView {
	private final Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries;

	public DynamicRegistryViewImpl(Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries) {
		this.registries = registries;
	}

	@Override
	public DynamicRegistryManager asDynamicRegistryManager() {
		return new DynamicRegistryManager.Frozen() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> Optional<Registry<T>> getLookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
				return Optional.ofNullable((Registry<T>) DynamicRegistryViewImpl.this.registries.get(resourceKey));
			}

			public Stream<RegistryEntry<?>> registries() {
				return DynamicRegistryViewImpl.this.stream()
						.map(this::entry);
			}

			private <T> RegistryEntry<T> entry(Registry<T> registry) {
				return new RegistryEntry<>(registry.getKey(), registry);
			}

			public Frozen freeze() {
				return this;
			}
		};
	}

	@Override
	public Stream<Registry<?>> stream() {
		return this.registries.values().stream();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<Registry<T>> getOptional(ResourceKey<? extends Registry<? extends T>> registryRef) {
		return Optional.ofNullable((Registry<T>) this.registries.get(registryRef));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void registerEntryAdded(ResourceKey<? extends Registry<? extends T>> registryRef, RegistryEntryAddedCallback<T> callback) {
		Registry<T> registry = (Registry<T>) this.registries.get(registryRef);

		if (registry != null) {
			RegistryEntryAddedCallback.event(registry).register(callback);
		}
	}
}

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
package com.bookkeepersmc.notebook.api.datagen.v1.provider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.event.registry.DynamicRegistries;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.impl.datagen.NotebookDatagenHelper;
import com.bookkeepersmc.notebook.impl.registry.sync.DynamicRegistriesImpl;

public abstract class DynamicRegistryDataProvider implements DataProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRegistryDataProvider.class);

	private final NotebookDataOutput output;
	private final CompletableFuture<HolderLookup.Provider> registriesFuture;

	public DynamicRegistryDataProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		this.output = output;
		this.registriesFuture = registriesFuture;
	}

	protected abstract void configure(HolderLookup.Provider registries, Entries entries);

	public static final class Entries {
		private final HolderLookup.Provider registries;
		// Registry ID -> Entries for that registry
		private final Map<ResourceLocation, RegistryEntries<?>> queuedEntries;
		private final String modId;

		@ApiStatus.Internal
		Entries(HolderLookup.Provider registries, String modId) {
			this.registries = registries;
			this.queuedEntries = DynamicRegistries.getDynamicRegistries().stream()
					// Some modded dynamic registries might not be in the wrapper lookup, filter them out
					.filter(e -> registries.lookup(e.key()).isPresent())
					.collect(Collectors.toMap(
							e -> e.key().location(),
							e -> RegistryEntries.create(registries, e)
					));
			this.modId = modId;
		}

		public HolderLookup.Provider getLookups() {
			return registries;
		}

		public <T> HolderGetter<T> getLookup(ResourceKey<? extends Registry<T>> registryKey) {
			return registries.lookupOrThrow(registryKey);
		}

		public HolderGetter<PlacedFeature> placedFeatures() {
			return getLookup(Registries.PLACED_FEATURE);
		}

		public HolderGetter<ConfiguredWorldCarver<?>> configuredCarvers() {
			return getLookup(Registries.CONFIGURED_CARVER);
		}

		public <T> Holder<T> ref(ResourceKey<T> key) {
			RegistryEntries<T> entries = getQueuedEntries(key);
			return Holder.Reference.createStandAlone(entries.lookup, key);
		}

		public <T> Holder<T> add(ResourceKey<T> key, T object) {
			return getQueuedEntries(key).add(key, object, null);
		}

		public <T> Holder<T> add(ResourceKey<T> key, T object, ResourceCondition... conditions) {
			return getQueuedEntries(key).add(key, object, conditions);
		}

		public <T> void add(Holder.Reference<T> object) {
			add(object.key(), object.value());
		}

		public <T> void add(Holder.Reference<T> object, ResourceCondition... conditions) {
			add(object.key(), object.value(), conditions);
		}

		public <T> Holder<T> add(HolderLookup.RegistryLookup<T> registry, ResourceKey<T> valueKey) {
			return add(valueKey, registry.getOrThrow(valueKey).value());
		}

		public <T> Holder<T> add(HolderLookup.RegistryLookup<T> registry, ResourceKey<T> valueKey, ResourceCondition... conditions) {
			return add(valueKey, registry.getOrThrow(valueKey).value(), conditions);
		}

		public <T> List<Holder<T>> addAll(HolderLookup.RegistryLookup<T> registry) {
			return registry.listElementIds()
					.filter(registryKey -> registryKey.location().getNamespace().equals(modId))
					.map(key -> add(registry, key))
					.toList();
		}

		@SuppressWarnings("unchecked")
		<T> RegistryEntries<T> getQueuedEntries(ResourceKey<T> key) {
			RegistryEntries<?> regEntries = queuedEntries.get(key.registry());

			if (regEntries == null) {
				throw new IllegalArgumentException("Registry " + key.registry() + " is not loaded from datapacks");
			}

			return (RegistryEntries<T>) regEntries;
		}
	}

	private record ConditionalEntry<T>(T value, @Nullable ResourceCondition... conditions) {
	}

	private static class RegistryEntries<T> {
		final HolderOwner<T> lookup;
		final ResourceKey<? extends Registry<T>> registry;
		final Codec<T> elementCodec;
		Map<ResourceKey<T>, ConditionalEntry<T>> entries = new IdentityHashMap<>();

		RegistryEntries(HolderOwner<T> lookup,
						ResourceKey<? extends Registry<T>> registry,
						Codec<T> elementCodec) {
			this.lookup = lookup;
			this.registry = registry;
			this.elementCodec = elementCodec;
		}

		static <T> RegistryEntries<T> create(HolderLookup.Provider lookups, RegistryDataLoader.RegistryData<T> loaderEntry) {
			HolderLookup.RegistryLookup<T> lookup = lookups.lookupOrThrow(loaderEntry.key());
			return new RegistryEntries<>(lookup, loaderEntry.key(), loaderEntry.elementCodec());
		}

		Holder<T> add(ResourceKey<T> key, T value, @Nullable ResourceCondition[] conditions) {
			if (entries.put(key, new ConditionalEntry<>(value, conditions)) != null) {
				throw new IllegalArgumentException("Trying to add registry key " + key + " more than once.");
			}

			return Holder.Reference.createStandAlone(lookup, key);
		}
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		return registriesFuture.thenCompose(registries -> {
			return CompletableFuture
					.supplyAsync(() -> {
						Entries entries = new Entries(registries, output.getModId());
						configure(registries, entries);
						return entries;
					})
					.thenCompose(entries -> {
						final RegistryOps<JsonElement> dynamicOps = registries.createSerializationContext(JsonOps.INSTANCE);
						ArrayList<CompletableFuture<?>> futures = new ArrayList<>();

						for (RegistryEntries<?> registryEntries : entries.queuedEntries.values()) {
							futures.add(writeRegistryEntries(writer, dynamicOps, registryEntries));
						}

						return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
					});
		});
	}

	private <T> CompletableFuture<?> writeRegistryEntries(CachedOutput writer, RegistryOps<JsonElement> ops, RegistryEntries<T> entries) {
		final ResourceKey<? extends Registry<T>> registry = entries.registry;
		final boolean shouldOmitNamespace = registry.location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) || !DynamicRegistriesImpl.NOTEBOOK_DYNAMIC_REGISTRY_KEYS.contains(registry);
		final String directoryName = shouldOmitNamespace ? registry.location().getPath() : registry.location().getNamespace() + "/" + registry.location().getPath();
		final PackOutput.PathProvider pathResolver = output.createPathProvider(PackOutput.Target.DATA_PACK, directoryName);
		final List<CompletableFuture<?>> futures = new ArrayList<>();

		for (Map.Entry<ResourceKey<T>, ConditionalEntry<T>> entry : entries.entries.entrySet()) {
			Path path = pathResolver.json(entry.getKey().location());
			futures.add(writeToPath(path, writer, ops, entries.elementCodec, entry.getValue().value(), entry.getValue().conditions()));
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private static <E> CompletableFuture<?> writeToPath(Path path, CachedOutput cache, DynamicOps<JsonElement> json, Encoder<E> encoder, E value, @Nullable ResourceCondition[] conditions) {
		Optional<JsonElement> optional = encoder.encodeStart(json, value).resultOrPartial((error) -> {
			LOGGER.error("Couldn't serialize element {}: {}", path, error);
		});

		if (optional.isPresent()) {
			JsonElement jsonElement = optional.get();

			if (conditions != null && conditions.length > 0) {
				if (!jsonElement.isJsonObject()) {
					throw new IllegalStateException("Cannot add conditions to " + path + ": JSON is a non-object value");
				} else {
					NotebookDatagenHelper.addConditions(jsonElement.getAsJsonObject(), conditions);
				}
			}

			return DataProvider.saveStable(cache, jsonElement, path);
		}

		return CompletableFuture.completedFuture(null);
	}
}

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
package com.bookkeepersmc.notebook.impl.datagen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.data.DataProvider;
import net.minecraft.registry.BootstrapContext;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistrySetBuilder;
import net.minecraft.registry.ResourceKey;
import net.minecraft.registry.VanillaDynamicRegistries;
import net.minecraft.util.Util;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.loader.api.entrypoint.EntrypointContainer;
import com.bookkeepersmc.notebook.api.datagen.v1.DataGeneratorEntrypoint;
import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataGenerator;
import com.bookkeepersmc.notebook.api.event.registry.DynamicRegistries;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceConditions;

public class NotebookDatagenHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(NotebookDatagenHelper.class);

	public static final boolean ENABLED = System.getProperty("notebook.datagen") != null;

	private static final String OUTPUT_DIR = System.getProperty("notebook.datagen.output-dir");

	private static final boolean STRICT_VALIDATION = System.getProperty("notebook.datagen.strict-validation") != null;

	@Nullable
	private static final String MOD_ID_FILTER = System.getProperty("notebook.datagen.modid");

	private static final String ENTRYPOINT_KEY = "datagen";

	private NotebookDatagenHelper() {
	}

	public static void run() {
		try {
			runInternal();
		} catch (Throwable t) {
			LOGGER.error(LogUtils.FATAL_MARKER, "Failed to run data generation", t);

			System.exit(-1);
		}
	}

	private static void runInternal() {
		Path outputDir = Paths.get(Objects.requireNonNull(OUTPUT_DIR, "No output dir provided with the 'notebook.datagen.output-dir' property"));

		List<EntrypointContainer<DataGeneratorEntrypoint>> datagenInitializers = NotebookLoader.getInstance()
				.getEntrypointContainers(ENTRYPOINT_KEY, DataGeneratorEntrypoint.class);

		if (datagenInitializers.isEmpty()) {
			LOGGER.warn("No data generator entrypoints are defined. Implement {} and add your class to the '{}' entrypoint key in your notebook.mod.json.",
					DataGeneratorEntrypoint.class.getName(), ENTRYPOINT_KEY);
		}

		// Ensure that the DataGeneratorEntrypoint is constructed on the main thread.
		final List<DataGeneratorEntrypoint> entrypoints = datagenInitializers.stream().map(EntrypointContainer::getEntrypoint).toList();
		CompletableFuture<HolderLookup.Provider> registriesFuture = CompletableFuture.supplyAsync(() -> createRegistryWrapper(entrypoints), Util.getMainWorkerExecutor());

		Object2IntOpenHashMap<String> jsonKeySortOrders = (Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS;
		Object2IntOpenHashMap<String> defaultJsonKeySortOrders = new Object2IntOpenHashMap<>(jsonKeySortOrders);

		for (EntrypointContainer<DataGeneratorEntrypoint> entrypointContainer : datagenInitializers) {
			final String id = entrypointContainer.getProvider().getMetadata().getId();

			if (MOD_ID_FILTER != null) {
				if (!id.equals(MOD_ID_FILTER)) {
					continue;
				}
			}

			LOGGER.info("Running data generator for {}", id);

			try {
				final DataGeneratorEntrypoint entrypoint = entrypointContainer.getEntrypoint();
				final String effectiveModId = entrypoint.getEffectiveModId();
				ModContainer modContainer = entrypointContainer.getProvider();

				HashSet<String> keys = new HashSet<>();
				entrypoint.addJsonKeySortOrders((key, value) -> {
					Objects.requireNonNull(key, "Tried to register a priority for a null key");
					jsonKeySortOrders.put(key, value);
					keys.add(key);
				});

				if (effectiveModId != null) {
					modContainer = NotebookLoader.getInstance().getModContainer(effectiveModId).orElseThrow(() -> new RuntimeException("Failed to find effective mod container for mod id (%s)".formatted(effectiveModId)));
				}

				NotebookDataGenerator dataGenerator = new NotebookDataGenerator(outputDir, modContainer, STRICT_VALIDATION, registriesFuture);
				entrypoint.onDataGeneratorInit(dataGenerator);
				dataGenerator.run();

				jsonKeySortOrders.keySet().removeAll(keys);
				jsonKeySortOrders.putAll(defaultJsonKeySortOrders);
			} catch (Throwable t) {
				throw new RuntimeException("Failed to run data generator from mod (%s)".formatted(id), t);
			}
		}
	}

	private static HolderLookup.Provider createRegistryWrapper(List<DataGeneratorEntrypoint> dataGeneratorInitializers) {
		// Build a list of all the RegistryBuilder's including vanilla's
		List<RegistrySetBuilder> builders = new ArrayList<>();
		builders.add(VanillaDynamicRegistries.BUILDER);

		for (DataGeneratorEntrypoint entrypoint : dataGeneratorInitializers) {
			final var registryBuilder = new RegistrySetBuilder();
			entrypoint.buildRegistry(registryBuilder);
			builders.add(registryBuilder);
		}

		class BuilderData {
			final ResourceKey key;
			List<RegistrySetBuilder.RegistryBootstrap<?>> bootstrapFunctions;
			Lifecycle lifecycle;

			BuilderData(ResourceKey key) {
				this.key = key;
				this.bootstrapFunctions = new ArrayList<>();
				this.lifecycle = Lifecycle.stable();
			}

			void with(RegistrySetBuilder.RegistryStub<?> registryInfo) {
				bootstrapFunctions.add(registryInfo.bootstrap());
				lifecycle = registryInfo.lifecycle().add(lifecycle);
			}

			void apply(RegistrySetBuilder builder) {
				builder.add(key, lifecycle, this::bootstrap);
			}

			void bootstrap(BootstrapContext registerable) {
				for (RegistrySetBuilder.RegistryBootstrap<?> function : bootstrapFunctions) {
					function.run(registerable);
				}
			}
		}

		Map<ResourceKey<?>, BuilderData> builderDataMap = new HashMap<>();

		for (RegistryLoader.DecodingData<?> key : DynamicRegistries.getDynamicRegistries()) {
			builderDataMap.computeIfAbsent(key.key(), BuilderData::new);
		}

		for (RegistrySetBuilder builder : builders) {
			for (RegistrySetBuilder.RegistryStub<?> info : builder.entries) {
				builderDataMap.computeIfAbsent(info.key(), BuilderData::new)
						.with(info);
			}
		}

		RegistrySetBuilder merged = new RegistrySetBuilder();

		for (BuilderData value : builderDataMap.values()) {
			value.apply(merged);
		}

		HolderLookup.Provider wrapperLookup = merged.build(DynamicRegistryManager.fromRegistryOfRegistries(BuiltInRegistries.ROOT));
		VanillaDynamicRegistries.validateBiomeFeatures(wrapperLookup);
		return wrapperLookup;
	}

	private static final Map<Object, ResourceCondition[]> CONDITIONS_MAP = new IdentityHashMap<>();

	public static void addConditions(Object object, ResourceCondition[] conditions) {
		CONDITIONS_MAP.merge(object, conditions, ArrayUtils::addAll);
	}

	@Nullable
	public static ResourceCondition[] consumeConditions(Object object) {
		return CONDITIONS_MAP.remove(object);
	}

	public static void addConditions(JsonObject baseObject, ResourceCondition... conditions) {
		if (baseObject.has(ResourceConditions.CONDITIONS_KEY)) {
			throw new IllegalArgumentException("Object already has a condition entry: " + baseObject);
		} else if (conditions == null || conditions.length == 0) {
			// Datagen might pass null conditions.
			return;
		}

		baseObject.add(ResourceConditions.CONDITIONS_KEY, ResourceCondition.LIST_CODEC.encodeStart(JsonOps.INSTANCE, Arrays.asList(conditions)).getOrThrow());
	}
}

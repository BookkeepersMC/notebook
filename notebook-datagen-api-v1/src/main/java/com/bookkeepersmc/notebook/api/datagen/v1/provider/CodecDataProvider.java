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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.data.DataPackOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ResourceKey;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;

public abstract class CodecDataProvider<T> implements DataProvider {
	private final DataPackOutput.PathResolver pathProvider;
	private final CompletableFuture<HolderLookup.Provider> provider;
	private final Codec<T> codec;

	protected CodecDataProvider(DataPackOutput.PathResolver pathProvider, CompletableFuture<HolderLookup.Provider> provider, Codec<T> codec) {
		this.pathProvider = pathProvider;
		this.provider = Objects.requireNonNull(provider);
		this.codec = codec;
	}

	protected CodecDataProvider(NotebookDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture, DataPackOutput.Type outputType, String directoryName, Codec<T> codec) {
		this(dataOutput.createPathResolver(outputType, directoryName), registriesFuture, codec);
	}

	protected CodecDataProvider(NotebookDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture, ResourceKey<? extends Registry<?>> key, Codec<T> codec) {
		this(dataOutput.createPathResolver(key), registriesFuture, codec);
	}

	@Override
	public CompletableFuture<?> run(DataWriter cachedOutput) {
		return this.provider.thenCompose(lookup -> {
			Map<Identifier, JsonElement> entries = new HashMap<>();
			RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);

			BiConsumer<Identifier, T> provider = (id, value) -> {
				JsonElement json = this.convert(id, value, ops);
				JsonElement existingJson = entries.put(id, json);

				if (existingJson != null) {
					throw new IllegalArgumentException("Duplicate entry " + id);
				}
			};

			this.configure(provider, lookup);
			return this.write(cachedOutput, entries);
		});
	}

	protected abstract void configure(BiConsumer<Identifier, T> provider, HolderLookup.Provider lookup);

	private JsonElement convert(Identifier id, T value, DynamicOps<JsonElement> ops) {
		DataResult<JsonElement> dataResult = this.codec.encodeStart(ops, value);
		return dataResult
				.mapError(mesage -> "Invalid entry %s: %s".formatted(id, mesage))
				.getOrThrow();
	}

	private CompletableFuture<?> write(DataWriter output, Map<Identifier, JsonElement> entries) {
		return CompletableFuture.allOf(entries.entrySet().stream().map(entry -> {
			Path path = this.pathProvider.resolveJsonFile(entry.getKey());
			return DataProvider.writeToPath(output, entry.getValue(), path);
		}).toArray(CompletableFuture[]::new));
	}
}

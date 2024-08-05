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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.impl.datagen.NotebookDatagenHelper;

public abstract class AdvancementDataProvider implements DataProvider {
	protected final NotebookDataOutput output;
	private final PackOutput.PathProvider pathResolver;
	private final CompletableFuture<HolderLookup.Provider> registryLookup;

	protected AdvancementDataProvider(NotebookDataOutput output,  CompletableFuture<HolderLookup.Provider> registryLookup) {
		this.output = output;
		this.pathResolver = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
		this.registryLookup = registryLookup;
	}

	public abstract void generateAdvancements(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer);


	protected Consumer<AdvancementHolder> withConditions(Consumer<AdvancementHolder> exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return advancementHolder -> {
			NotebookDatagenHelper.addConditions(advancementHolder, conditions);
			exporter.accept(advancementHolder);
		};
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		return this.registryLookup.thenCompose(lookup -> {
			final Set<ResourceLocation> identifiers = Sets.newHashSet();
			final Set<AdvancementHolder> advancements = Sets.newHashSet();

			generateAdvancements(lookup, advancements::add);

			RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
			final List<CompletableFuture<?>> futures = new ArrayList<>();

			for (AdvancementHolder advancement : advancements) {
				if (!identifiers.add(advancement.id())) {
					throw new IllegalStateException("Duplicate advancement " + advancement.id());
				}

				JsonObject advancementJson = Advancement.CODEC.encodeStart(ops, advancement.value()).getOrThrow(IllegalStateException::new).getAsJsonObject();
				NotebookDatagenHelper.addConditions(advancementJson, NotebookDatagenHelper.consumeConditions(advancement));
				futures.add(DataProvider.saveStable(writer, advancementJson, getOutputPath(advancement)));
			}

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		});
	}

	private Path getOutputPath(AdvancementHolder advancement) {
		return pathResolver.json(advancement.id());
	}

	@Override
	public String getName() {
		return "Advancements";
	}
}

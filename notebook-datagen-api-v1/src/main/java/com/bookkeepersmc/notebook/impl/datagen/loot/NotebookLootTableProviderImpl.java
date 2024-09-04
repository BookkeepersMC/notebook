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
package com.bookkeepersmc.notebook.impl.datagen.loot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.LootTableDataProvider;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.impl.datagen.NotebookDatagenHelper;

public final class NotebookLootTableProviderImpl {
	public static CompletableFuture<?> run(
			DataWriter writer,
			LootTableDataProvider provider,
			LootContextType lootContextType,
			NotebookDataOutput dataOutput,
			CompletableFuture<HolderLookup.Provider> registryLookup) {
		HashMap<Identifier, LootTable> builders = Maps.newHashMap();
		HashMap<Identifier, ResourceCondition[]> conditionMap = new HashMap<>();

		return registryLookup.thenCompose(lookup -> {
			provider.generate((registryKey, builder) -> {
				ResourceCondition[] conditions = NotebookDatagenHelper.consumeConditions(builder);
				conditionMap.put(registryKey.getValue(), conditions);

				if (builders.put(registryKey.getValue(), builder.type(lootContextType).build()) != null) {
					throw new IllegalStateException("Duplicate loot table " + registryKey.getValue());
				}
			});

			RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
			final List<CompletableFuture<?>> futures = new ArrayList<>();

			for (Map.Entry<Identifier, LootTable> entry : builders.entrySet()) {
				JsonObject tableJson = (JsonObject) LootTable.field_50021.encodeStart(ops, entry.getValue()).getOrThrow(IllegalStateException::new);
				NotebookDatagenHelper.addConditions(tableJson, conditionMap.remove(entry.getKey()));
				futures.add(DataProvider.writeToPath(writer, tableJson, getOutputPath(dataOutput, entry.getKey())));
			}

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		});
	}

	private static Path getOutputPath(NotebookDataOutput dataOutput, Identifier lootTableId) {
		return dataOutput.method_60917(Registries.LOOT_TABLE).resolveJsonFile(lootTableId);
	}

	private NotebookLootTableProviderImpl() {
	}
}

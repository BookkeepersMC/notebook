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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.common.collect.Sets;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.impl.datagen.loot.NotebookLootTableProviderImpl;

public abstract class BlockLootTableDataProvider extends BlockLootSubProvider implements LootTableDataProvider {
	private final NotebookDataOutput output;
	private final Set<ResourceLocation> excludedFromStrictValidation = new HashSet<>();
	private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;

	protected BlockLootTableDataProvider(NotebookDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registryLookup.join());
		this.output = dataOutput;
		this.registryLookupFuture = registryLookup;
	}

	/**
	 * Implement this method to add block drops.
	 *
	 * <p>Use the range of {@link BlockLootSubProvider#dropSelf} methods to generate block drops.
	 */
	@Override
	public abstract void generate();

	/**
	 * Disable strict validation for the passed block.
	 */
	public void excludeFromStrictValidation(Block block) {
		excludedFromStrictValidation.add(BuiltInRegistries.BLOCK.getKey(block));
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		generate();

		for (Map.Entry<ResourceKey<LootTable>, LootTable.Builder> entry : map.entrySet()) {
			ResourceKey<LootTable> registryKey = entry.getKey();

			if (registryKey == BuiltInLootTables.EMPTY) {
				continue;
			}

			biConsumer.accept(registryKey, entry.getValue());
		}

		if (output.isStrictValidationEnabled()) {
			Set<ResourceLocation> missing = Sets.newHashSet();

			for (ResourceLocation blockId : BuiltInRegistries.BLOCK.keySet()) {
				if (blockId.getNamespace().equals(output.getModId())) {
					ResourceKey<LootTable> blockLootTableId = BuiltInRegistries.BLOCK.get(blockId).getLootTable();

					if (blockLootTableId.location().getNamespace().equals(output.getModId())) {
						if (!map.containsKey(blockLootTableId)) {
							missing.add(blockId);
						}
					}
				}
			}

			missing.removeAll(excludedFromStrictValidation);

			if (!missing.isEmpty()) {
				throw new IllegalStateException("Missing loot table(s) for %s".formatted(missing));
			}
		}
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		return NotebookLootTableProviderImpl.run(writer, this, LootContextParamSets.BLOCK, output, registryLookupFuture);
	}

	@Override
	public String getName() {
		return "Block Loot Tables";
	}
}

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.data.DataWriter;
import net.minecraft.data.server.loot_table.BlockLootTableGenerator;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.ResourceKey;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.impl.datagen.loot.NotebookLootTableProviderImpl;

public abstract class BlockLootTableDataProvider extends BlockLootTableGenerator implements LootTableDataProvider {
	private final NotebookDataOutput output;
	private final Set<Identifier> excludedFromStrictValidation = new HashSet<>();
	private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;

	protected BlockLootTableDataProvider(NotebookDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(Collections.emptySet(), FeatureFlags.MAIN_REGISTRY.setOf(), registryLookup.join());
		this.output = dataOutput;
		this.registryLookupFuture = registryLookup;
	}

	/**
	 * Implement this method to add block drops.
	 *
	 * <p>Use the range of {@link BlockLootTableGenerator#addDrop} methods to generate block drops.
	 */
	@Override
	public abstract void generate();

	/**
	 * Disable strict validation for the passed block.
	 */
	public void excludeFromStrictValidation(Block block) {
		excludedFromStrictValidation.add(BuiltInRegistries.BLOCK.getId(block));
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		generate();

		for (Map.Entry<ResourceKey<LootTable>, LootTable.Builder> entry : lootTables.entrySet()) {
			ResourceKey<LootTable> registryKey = entry.getKey();

			biConsumer.accept(registryKey, entry.getValue());
		}

		if (output.isStrictValidationEnabled()) {
			Set<Identifier> missing = Sets.newHashSet();

			for (Identifier blockId : BuiltInRegistries.BLOCK.getIds()) {
				if (blockId.getNamespace().equals(output.getModId())) {
					Optional<ResourceKey<LootTable>> blockLootTableId = BuiltInRegistries.BLOCK.get(blockId).getLootTableId();

					if (blockLootTableId.isPresent() && blockLootTableId.get().getValue().getNamespace().equals(output.getModId())) {
						if (!lootTables.containsKey(blockLootTableId.get())) {
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
	public CompletableFuture<?> run(DataWriter writer) {
		return NotebookLootTableProviderImpl.run(writer, this, LootContextTypes.BLOCK, output, registryLookupFuture);
	}

	@Override
	public String getDescription() {
		return "Block Loot Tables";
	}
}

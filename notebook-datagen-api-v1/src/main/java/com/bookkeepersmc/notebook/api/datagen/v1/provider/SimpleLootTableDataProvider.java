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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.impl.datagen.loot.NotebookLootTableProviderImpl;

public abstract class SimpleLootTableDataProvider implements LootTableDataProvider {
	protected final NotebookDataOutput output;
	private final CompletableFuture<HolderLookup.Provider> registryLookup;
	protected final LootContextParamSet lootContextType;

	public SimpleLootTableDataProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup, LootContextParamSet lootContextType) {
		this.output = output;
		this.registryLookup = registryLookup;
		this.lootContextType = lootContextType;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		return NotebookLootTableProviderImpl.run(writer, this, lootContextType, output, registryLookup);
	}

	@Override
	public String getName() {
		return Objects.requireNonNull(LootContextParamSets.REGISTRY.inverse().get(lootContextType), "Could not get id for loot context type") + " Loot Table";
	}
}

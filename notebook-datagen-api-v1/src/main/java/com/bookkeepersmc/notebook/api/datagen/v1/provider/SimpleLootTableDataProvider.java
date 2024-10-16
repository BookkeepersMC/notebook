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

import net.minecraft.data.DataWriter;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.HolderLookup;
import net.minecraft.util.context.ContextKeySet;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.impl.datagen.loot.NotebookLootTableProviderImpl;

public abstract class SimpleLootTableDataProvider implements LootTableDataProvider {
	protected final NotebookDataOutput output;
	private final CompletableFuture<HolderLookup.Provider> registryLookup;
	protected final ContextKeySet contextKeySet;

	public SimpleLootTableDataProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup, ContextKeySet contextKeySet) {
		this.output = output;
		this.registryLookup = registryLookup;
		this.contextKeySet = contextKeySet;
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return NotebookLootTableProviderImpl.run(writer, this, contextKeySet, output, registryLookup);
	}

	@Override
	public String getDescription() {
		return Objects.requireNonNull(LootContextTypes.MAP.inverse().get(contextKeySet), "Could not get id for loot context type") + " Loot Table";
	}
}

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

import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.data.server.loot_table.BlockLootTableGenerator;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.loot.LootTable;

import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.impl.datagen.NotebookDatagenHelper;
import com.bookkeepersmc.notebook.mixin.datagen.loot.BlockLootTableGeneratorAccessor;

public class ConditionBlockLootTableGenerator extends BlockLootTableGenerator {
	private final BlockLootTableGenerator parent;
	private final ResourceCondition[] conditions;

	public ConditionBlockLootTableGenerator(BlockLootTableGenerator parent, ResourceCondition[] conditions) {
		super(Collections.emptySet(), FeatureFlags.MAIN_REGISTRY.setOf(), ((BlockLootTableGeneratorAccessor) parent).getProvider());

		this.parent = parent;
		this.conditions = conditions;
	}

	@Override
	public void generate() {
		throw new UnsupportedOperationException("generate() should not be called.");
	}

	@Override
	public void add(Block block, LootTable.Builder lootTable) {
		NotebookDatagenHelper.addConditions(lootTable, conditions);
		this.parent.add(block, lootTable);
	}
}

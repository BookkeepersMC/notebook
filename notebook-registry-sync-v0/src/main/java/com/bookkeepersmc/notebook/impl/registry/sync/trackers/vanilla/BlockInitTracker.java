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
package com.bookkeepersmc.notebook.impl.registry.sync.trackers.vanilla;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.bookkeepersmc.notebook.api.event.registry.RegistryEntryAddedCallback;
import com.bookkeepersmc.notebook.mixin.registry.sync.DebugChunkGeneratorAccessor;

public final class BlockInitTracker implements RegistryEntryAddedCallback<Block> {
	private final Registry<Block> registry;

	private BlockInitTracker(Registry<Block> registry) {
		this.registry = registry;
	}

	public static void register(Registry<Block> registry) {
		BlockInitTracker tracker = new BlockInitTracker(registry);
		RegistryEntryAddedCallback.event(registry).register(tracker);
	}

	@Override
	public void onEntryAdded(int rawId, ResourceLocation id, Block object) {
		// if false, getLootTableKey() will generate an invalid loot table key
		assert id.equals(registry.getKey(object));

		object.getLootTable();
	}

	public static void postFreeze() {
		final List<BlockState> blockStateList = BuiltInRegistries.BLOCK.stream()
				.flatMap((block) -> block.getStateDefinition().getPossibleStates().stream())
				.toList();

		final int xLength = Mth.ceil(Mth.sqrt(blockStateList.size()));
		final int zLength = Mth.ceil(blockStateList.size() / (float) xLength);

		DebugChunkGeneratorAccessor.setALL_BLOCKS(blockStateList);
		DebugChunkGeneratorAccessor.setGRID_WIDTH(xLength);
		DebugChunkGeneratorAccessor.setGRID_HEIGHT(zLength);
	}
}

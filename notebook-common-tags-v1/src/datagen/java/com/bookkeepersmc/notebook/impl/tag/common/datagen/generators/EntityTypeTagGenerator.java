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
package com.bookkeepersmc.notebook.impl.tag.common.datagen.generators;

import java.util.concurrent.CompletableFuture;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.tag.EntityTypeTags;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.TagDataProvider;
import com.bookkeepersmc.notebook.api.tag.common.v1.ConventionalEntityTypeTags;

public final class EntityTypeTagGenerator extends TagDataProvider.EntityTypeTagProvider {
	public EntityTypeTagGenerator(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(output, completableFuture);
	}

	@Override
	protected void configure(HolderLookup.Provider wrapperLookup) {
		getOrCreateTagBuilder(ConventionalEntityTypeTags.BOSSES)
				.add(EntityType.WITHER)
				.add(EntityType.ENDER_DRAGON);
		getOrCreateTagBuilder(ConventionalEntityTypeTags.MINECARTS)
				.add(EntityType.MINECART)
				.add(EntityType.TNT_MINECART)
				.add(EntityType.CHEST_MINECART)
				.add(EntityType.FURNACE_MINECART)
				.add(EntityType.COMMAND_BLOCK_MINECART)
				.add(EntityType.HOPPER_MINECART)
				.add(EntityType.SPAWNER_MINECART);
		getOrCreateTagBuilder(ConventionalEntityTypeTags.BOATS)
				.addOptionalTag(EntityTypeTags.BOAT)
				.add(EntityType.OAK_CHEST_BOAT)
				.add(EntityType.SPRUCE_CHEST_BOAT)
				.add(EntityType.BIRCH_CHEST_BOAT)
				.add(EntityType.JUNGLE_CHEST_BOAT)
				.add(EntityType.ACACIA_CHEST_BOAT)
				.add(EntityType.CHERRY_CHEST_BOAT)
				.add(EntityType.PALE_OAK_CHEST_BOAT)
				.add(EntityType.DARK_OAK_CHEST_BOAT)
				.add(EntityType.MANGROVE_CHEST_BOAT)
				.add(EntityType.BAMBOO_CHEST_RAFT);
		getOrCreateTagBuilder(ConventionalEntityTypeTags.CAPTURING_NOT_SUPPORTED);
		getOrCreateTagBuilder(ConventionalEntityTypeTags.TELEPORTING_NOT_SUPPORTED);
	}
}

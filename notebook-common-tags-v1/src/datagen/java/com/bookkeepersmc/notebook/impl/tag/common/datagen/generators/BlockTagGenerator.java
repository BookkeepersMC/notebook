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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.TagDataProvider;
import com.bookkeepersmc.notebook.api.tag.common.v1.ConventionalBlockTags;
import com.bookkeepersmc.notebook.api.tag.common.v1.TagUtil;

public final class BlockTagGenerator extends TagDataProvider.BlockTagProvider {
	static List<Block> VILLAGER_JOB_SITES = List.of(
			Blocks.BARREL,
			Blocks.BLAST_FURNACE,
			Blocks.BREWING_STAND,
			Blocks.CARTOGRAPHY_TABLE,
			Blocks.CAULDRON,
			Blocks.LAVA_CAULDRON,
			Blocks.WATER_CAULDRON,
			Blocks.POWDER_SNOW_CAULDRON,
			Blocks.COMPOSTER,
			Blocks.FLETCHING_TABLE,
			Blocks.GRINDSTONE,
			Blocks.LECTERN,
			Blocks.LOOM,
			Blocks.SMITHING_TABLE,
			Blocks.SMOKER,
			Blocks.STONECUTTER
	);

	public BlockTagGenerator(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		tag(ConventionalBlockTags.STONES)
				.add(Blocks.STONE)
				.add(Blocks.ANDESITE)
				.add(Blocks.DIORITE)
				.add(Blocks.GRANITE)
				.add(Blocks.TUFF)
				.add(Blocks.DEEPSLATE);
		tag(ConventionalBlockTags.COBBLESTONES)
				.add(Blocks.COBBLESTONE)
				.add(Blocks.MOSSY_COBBLESTONE)
				.add(Blocks.COBBLED_DEEPSLATE)
				.add(Blocks.INFESTED_COBBLESTONE);

		tag(ConventionalBlockTags.QUARTZ_ORES)
				.add(Blocks.NETHER_QUARTZ_ORE);
		tag(ConventionalBlockTags.NETHERITE_SCRAP_ORES)
				.add(Blocks.ANCIENT_DEBRIS);
		tag(ConventionalBlockTags.ORES)
				.addOptionalTag(BlockTags.COAL_ORES)
				.addOptionalTag(BlockTags.COPPER_ORES)
				.addOptionalTag(BlockTags.DIAMOND_ORES)
				.addOptionalTag(BlockTags.EMERALD_ORES)
				.addOptionalTag(BlockTags.GOLD_ORES)
				.addOptionalTag(BlockTags.IRON_ORES)
				.addOptionalTag(BlockTags.LAPIS_ORES)
				.addOptionalTag(ConventionalBlockTags.NETHERITE_SCRAP_ORES)
				.addOptionalTag(BlockTags.REDSTONE_ORES)
				.addOptionalTag(ConventionalBlockTags.QUARTZ_ORES);

		tag(ConventionalBlockTags.WOODEN_CHESTS)
				.add(Blocks.CHEST)
				.add(Blocks.TRAPPED_CHEST);
		tag(ConventionalBlockTags.CHESTS)
				.addTag(ConventionalBlockTags.WOODEN_CHESTS)
				.add(Blocks.ENDER_CHEST);
		tag(ConventionalBlockTags.BOOKSHELVES)
				.add(Blocks.BOOKSHELF);

		generateGlassTags();
		generateGlazeTerracottaTags();
		generateConcreteTags();
		tag(ConventionalBlockTags.WOODEN_BARRELS)
				.add(Blocks.BARREL);
		tag(ConventionalBlockTags.BARRELS)
				.addTag(ConventionalBlockTags.WOODEN_BARRELS);

		generateBuddingTags();
		generateSandstoneTags();
		generateDyedTags();
		generateStorageTags();
		generateHeadTags();
		generateMiscTags();
		generateBackwardsCompatTags();
	}

	private void generateMiscTags() {
		tag(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
				.add(Blocks.CRAFTING_TABLE);
		tag(ConventionalBlockTags.PLAYER_WORKSTATIONS_FURNACES)
				.add(Blocks.FURNACE);

		VILLAGER_JOB_SITES.forEach(tag(ConventionalBlockTags.VILLAGER_JOB_SITES)::add);

		tag(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED); // Generate tag so others can see it exists through JSON.

		tag(ConventionalBlockTags.ROPES); // Generate tag so others can see it exists through JSON.

		tag(ConventionalBlockTags.CHAINS)
				.add(Blocks.CHAIN);

		tag(ConventionalBlockTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}

	private void generateSandstoneTags() {
		tag(ConventionalBlockTags.SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_BLOCKS);
		tag(ConventionalBlockTags.SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_SLABS);
		tag(ConventionalBlockTags.SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.addOptionalTag(ConventionalBlockTags.RED_SANDSTONE_STAIRS);

		tag(ConventionalBlockTags.RED_SANDSTONE_BLOCKS)
				.add(Blocks.RED_SANDSTONE)
				.add(Blocks.CUT_RED_SANDSTONE)
				.add(Blocks.SMOOTH_RED_SANDSTONE)
				.add(Blocks.CHISELED_RED_SANDSTONE);
		tag(ConventionalBlockTags.RED_SANDSTONE_SLABS)
				.add(Blocks.RED_SANDSTONE_SLAB)
				.add(Blocks.CUT_RED_SANDSTONE_SLAB)
				.add(Blocks.SMOOTH_RED_SANDSTONE_SLAB);
		tag(ConventionalBlockTags.RED_SANDSTONE_STAIRS)
				.add(Blocks.RED_SANDSTONE_STAIRS)
				.add(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);

		tag(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS)
				.add(Blocks.SANDSTONE)
				.add(Blocks.CUT_SANDSTONE)
				.add(Blocks.SMOOTH_SANDSTONE)
				.add(Blocks.CHISELED_SANDSTONE);
		tag(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS)
				.add(Blocks.SANDSTONE_SLAB)
				.add(Blocks.CUT_SANDSTONE_SLAB)
				.add(Blocks.SMOOTH_SANDSTONE_SLAB);
		tag(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS)
				.add(Blocks.SANDSTONE_STAIRS)
				.add(Blocks.SMOOTH_SANDSTONE_STAIRS);
	}

	private void generateBuddingTags() {
		tag(ConventionalBlockTags.BUDDING_BLOCKS)
				.add(Blocks.BUDDING_AMETHYST);
		tag(ConventionalBlockTags.BUDS)
				.add(Blocks.SMALL_AMETHYST_BUD)
				.add(Blocks.MEDIUM_AMETHYST_BUD)
				.add(Blocks.LARGE_AMETHYST_BUD);
		tag(ConventionalBlockTags.CLUSTERS)
				.add(Blocks.AMETHYST_CLUSTER);
	}

	private void generateGlassTags() {
		tag(ConventionalBlockTags.GLASS_BLOCKS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS_TINTED);
		tag(ConventionalBlockTags.GLASS_BLOCKS_COLORLESS)
				.add(Blocks.GLASS);
		tag(ConventionalBlockTags.GLASS_BLOCKS_CHEAP)
				.add(Blocks.GLASS)
				.add(Blocks.WHITE_STAINED_GLASS)
				.add(Blocks.ORANGE_STAINED_GLASS)
				.add(Blocks.MAGENTA_STAINED_GLASS)
				.add(Blocks.LIGHT_BLUE_STAINED_GLASS)
				.add(Blocks.YELLOW_STAINED_GLASS)
				.add(Blocks.LIME_STAINED_GLASS)
				.add(Blocks.PINK_STAINED_GLASS)
				.add(Blocks.GRAY_STAINED_GLASS)
				.add(Blocks.LIGHT_GRAY_STAINED_GLASS)
				.add(Blocks.CYAN_STAINED_GLASS)
				.add(Blocks.PURPLE_STAINED_GLASS)
				.add(Blocks.BLUE_STAINED_GLASS)
				.add(Blocks.BROWN_STAINED_GLASS)
				.add(Blocks.GREEN_STAINED_GLASS)
				.add(Blocks.BLACK_STAINED_GLASS)
				.add(Blocks.RED_STAINED_GLASS);
		tag(ConventionalBlockTags.GLASS_BLOCKS_TINTED)
				.add(Blocks.TINTED_GLASS);
		tag(ConventionalBlockTags.GLASS_PANES)
				.add(Blocks.WHITE_STAINED_GLASS_PANE)
				.add(Blocks.ORANGE_STAINED_GLASS_PANE)
				.add(Blocks.MAGENTA_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
				.add(Blocks.YELLOW_STAINED_GLASS_PANE)
				.add(Blocks.LIME_STAINED_GLASS_PANE)
				.add(Blocks.PINK_STAINED_GLASS_PANE)
				.add(Blocks.GRAY_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
				.add(Blocks.CYAN_STAINED_GLASS_PANE)
				.add(Blocks.PURPLE_STAINED_GLASS_PANE)
				.add(Blocks.BLUE_STAINED_GLASS_PANE)
				.add(Blocks.BROWN_STAINED_GLASS_PANE)
				.add(Blocks.GREEN_STAINED_GLASS_PANE)
				.add(Blocks.BLACK_STAINED_GLASS_PANE)
				.add(Blocks.RED_STAINED_GLASS_PANE)
				.addOptionalTag(ConventionalBlockTags.GLASS_PANES_COLORLESS);
		tag(ConventionalBlockTags.GLASS_PANES_COLORLESS)
				.add(Blocks.GLASS_PANE);
	}

	private void generateGlazeTerracottaTags() {
		tag(ConventionalBlockTags.GLAZED_TERRACOTTAS)
				.add(Blocks.WHITE_GLAZED_TERRACOTTA)
				.add(Blocks.ORANGE_GLAZED_TERRACOTTA)
				.add(Blocks.MAGENTA_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.YELLOW_GLAZED_TERRACOTTA)
				.add(Blocks.LIME_GLAZED_TERRACOTTA)
				.add(Blocks.PINK_GLAZED_TERRACOTTA)
				.add(Blocks.GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.CYAN_GLAZED_TERRACOTTA)
				.add(Blocks.PURPLE_GLAZED_TERRACOTTA)
				.add(Blocks.BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.BROWN_GLAZED_TERRACOTTA)
				.add(Blocks.GREEN_GLAZED_TERRACOTTA)
				.add(Blocks.BLACK_GLAZED_TERRACOTTA)
				.add(Blocks.RED_GLAZED_TERRACOTTA);
		tag(ConventionalBlockTags.GLAZED_TERRACOTTA)
				.add(Blocks.WHITE_GLAZED_TERRACOTTA)
				.add(Blocks.ORANGE_GLAZED_TERRACOTTA)
				.add(Blocks.MAGENTA_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.YELLOW_GLAZED_TERRACOTTA)
				.add(Blocks.LIME_GLAZED_TERRACOTTA)
				.add(Blocks.PINK_GLAZED_TERRACOTTA)
				.add(Blocks.GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.CYAN_GLAZED_TERRACOTTA)
				.add(Blocks.PURPLE_GLAZED_TERRACOTTA)
				.add(Blocks.BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.BROWN_GLAZED_TERRACOTTA)
				.add(Blocks.GREEN_GLAZED_TERRACOTTA)
				.add(Blocks.BLACK_GLAZED_TERRACOTTA)
				.add(Blocks.RED_GLAZED_TERRACOTTA);
	}

	private void generateConcreteTags() {
		tag(ConventionalBlockTags.CONCRETES)
				.add(Blocks.WHITE_CONCRETE)
				.add(Blocks.ORANGE_CONCRETE)
				.add(Blocks.MAGENTA_CONCRETE)
				.add(Blocks.LIGHT_BLUE_CONCRETE)
				.add(Blocks.YELLOW_CONCRETE)
				.add(Blocks.LIME_CONCRETE)
				.add(Blocks.PINK_CONCRETE)
				.add(Blocks.GRAY_CONCRETE)
				.add(Blocks.LIGHT_GRAY_CONCRETE)
				.add(Blocks.CYAN_CONCRETE)
				.add(Blocks.PURPLE_CONCRETE)
				.add(Blocks.BLUE_CONCRETE)
				.add(Blocks.BROWN_CONCRETE)
				.add(Blocks.GREEN_CONCRETE)
				.add(Blocks.BLACK_CONCRETE)
				.add(Blocks.RED_CONCRETE);
		tag(ConventionalBlockTags.CONCRETE)
				.add(Blocks.WHITE_CONCRETE)
				.add(Blocks.ORANGE_CONCRETE)
				.add(Blocks.MAGENTA_CONCRETE)
				.add(Blocks.LIGHT_BLUE_CONCRETE)
				.add(Blocks.YELLOW_CONCRETE)
				.add(Blocks.LIME_CONCRETE)
				.add(Blocks.PINK_CONCRETE)
				.add(Blocks.GRAY_CONCRETE)
				.add(Blocks.LIGHT_GRAY_CONCRETE)
				.add(Blocks.CYAN_CONCRETE)
				.add(Blocks.PURPLE_CONCRETE)
				.add(Blocks.BLUE_CONCRETE)
				.add(Blocks.BROWN_CONCRETE)
				.add(Blocks.GREEN_CONCRETE)
				.add(Blocks.BLACK_CONCRETE)
				.add(Blocks.RED_CONCRETE);
	}

	private void generateDyedTags() {
		tag(ConventionalBlockTags.BLACK_DYED)
				.add(Blocks.BLACK_BANNER).add(Blocks.BLACK_BED).add(Blocks.BLACK_CANDLE).add(Blocks.BLACK_CARPET)
				.add(Blocks.BLACK_CONCRETE).add(Blocks.BLACK_CONCRETE_POWDER).add(Blocks.BLACK_GLAZED_TERRACOTTA)
				.add(Blocks.BLACK_SHULKER_BOX).add(Blocks.BLACK_STAINED_GLASS).add(Blocks.BLACK_STAINED_GLASS_PANE)
				.add(Blocks.BLACK_TERRACOTTA).add(Blocks.BLACK_WALL_BANNER).add(Blocks.BLACK_WOOL);

		tag(ConventionalBlockTags.BLUE_DYED)
				.add(Blocks.BLUE_BANNER).add(Blocks.BLUE_BED).add(Blocks.BLUE_CANDLE).add(Blocks.BLUE_CARPET)
				.add(Blocks.BLUE_CONCRETE).add(Blocks.BLUE_CONCRETE_POWDER).add(Blocks.BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.BLUE_SHULKER_BOX).add(Blocks.BLUE_STAINED_GLASS).add(Blocks.BLUE_STAINED_GLASS_PANE)
				.add(Blocks.BLUE_TERRACOTTA).add(Blocks.BLUE_WALL_BANNER).add(Blocks.BLUE_WOOL);

		tag(ConventionalBlockTags.BROWN_DYED)
				.add(Blocks.BROWN_BANNER).add(Blocks.BROWN_BED).add(Blocks.BROWN_CANDLE).add(Blocks.BROWN_CARPET)
				.add(Blocks.BROWN_CONCRETE).add(Blocks.BROWN_CONCRETE_POWDER).add(Blocks.BROWN_GLAZED_TERRACOTTA)
				.add(Blocks.BROWN_SHULKER_BOX).add(Blocks.BROWN_STAINED_GLASS).add(Blocks.BROWN_STAINED_GLASS_PANE)
				.add(Blocks.BROWN_TERRACOTTA).add(Blocks.BROWN_WALL_BANNER).add(Blocks.BROWN_WOOL);

		tag(ConventionalBlockTags.CYAN_DYED)
				.add(Blocks.CYAN_BANNER).add(Blocks.CYAN_BED).add(Blocks.CYAN_CANDLE).add(Blocks.CYAN_CARPET)
				.add(Blocks.CYAN_CONCRETE).add(Blocks.CYAN_CONCRETE_POWDER).add(Blocks.CYAN_GLAZED_TERRACOTTA)
				.add(Blocks.CYAN_SHULKER_BOX).add(Blocks.CYAN_STAINED_GLASS).add(Blocks.CYAN_STAINED_GLASS_PANE)
				.add(Blocks.CYAN_TERRACOTTA).add(Blocks.CYAN_WALL_BANNER).add(Blocks.CYAN_WOOL);

		tag(ConventionalBlockTags.GRAY_DYED)
				.add(Blocks.GRAY_BANNER).add(Blocks.GRAY_BED).add(Blocks.GRAY_CANDLE).add(Blocks.GRAY_CARPET)
				.add(Blocks.GRAY_CONCRETE).add(Blocks.GRAY_CONCRETE_POWDER).add(Blocks.GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.GRAY_SHULKER_BOX).add(Blocks.GRAY_STAINED_GLASS).add(Blocks.GRAY_STAINED_GLASS_PANE)
				.add(Blocks.GRAY_TERRACOTTA).add(Blocks.GRAY_WALL_BANNER).add(Blocks.GRAY_WOOL);

		tag(ConventionalBlockTags.GREEN_DYED)
				.add(Blocks.GREEN_BANNER).add(Blocks.GREEN_BED).add(Blocks.GREEN_CANDLE).add(Blocks.GREEN_CARPET)
				.add(Blocks.GREEN_CONCRETE).add(Blocks.GREEN_CONCRETE_POWDER).add(Blocks.GREEN_GLAZED_TERRACOTTA)
				.add(Blocks.GREEN_SHULKER_BOX).add(Blocks.GREEN_STAINED_GLASS).add(Blocks.GREEN_STAINED_GLASS_PANE)
				.add(Blocks.GREEN_TERRACOTTA).add(Blocks.GREEN_WALL_BANNER).add(Blocks.GREEN_WOOL);

		tag(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.add(Blocks.LIGHT_BLUE_BANNER).add(Blocks.LIGHT_BLUE_BED).add(Blocks.LIGHT_BLUE_CANDLE).add(Blocks.LIGHT_BLUE_CARPET)
				.add(Blocks.LIGHT_BLUE_CONCRETE).add(Blocks.LIGHT_BLUE_CONCRETE_POWDER).add(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_BLUE_SHULKER_BOX).add(Blocks.LIGHT_BLUE_STAINED_GLASS).add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_BLUE_TERRACOTTA).add(Blocks.LIGHT_BLUE_WALL_BANNER).add(Blocks.LIGHT_BLUE_WOOL);

		tag(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.add(Blocks.LIGHT_GRAY_BANNER).add(Blocks.LIGHT_GRAY_BED).add(Blocks.LIGHT_GRAY_CANDLE).add(Blocks.LIGHT_GRAY_CARPET)
				.add(Blocks.LIGHT_GRAY_CONCRETE).add(Blocks.LIGHT_GRAY_CONCRETE_POWDER).add(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_SHULKER_BOX).add(Blocks.LIGHT_GRAY_STAINED_GLASS).add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
				.add(Blocks.LIGHT_GRAY_TERRACOTTA).add(Blocks.LIGHT_GRAY_WALL_BANNER).add(Blocks.LIGHT_GRAY_WOOL);

		tag(ConventionalBlockTags.LIME_DYED)
				.add(Blocks.LIME_BANNER).add(Blocks.LIME_BED).add(Blocks.LIME_CANDLE).add(Blocks.LIME_CARPET)
				.add(Blocks.LIME_CONCRETE).add(Blocks.LIME_CONCRETE_POWDER).add(Blocks.LIME_GLAZED_TERRACOTTA)
				.add(Blocks.LIME_SHULKER_BOX).add(Blocks.LIME_STAINED_GLASS).add(Blocks.LIME_STAINED_GLASS_PANE)
				.add(Blocks.LIME_TERRACOTTA).add(Blocks.LIME_WALL_BANNER).add(Blocks.LIME_WOOL);

		tag(ConventionalBlockTags.MAGENTA_DYED)
				.add(Blocks.MAGENTA_BANNER).add(Blocks.MAGENTA_BED).add(Blocks.MAGENTA_CANDLE).add(Blocks.MAGENTA_CARPET)
				.add(Blocks.MAGENTA_CONCRETE).add(Blocks.MAGENTA_CONCRETE_POWDER).add(Blocks.MAGENTA_GLAZED_TERRACOTTA)
				.add(Blocks.MAGENTA_SHULKER_BOX).add(Blocks.MAGENTA_STAINED_GLASS).add(Blocks.MAGENTA_STAINED_GLASS_PANE)
				.add(Blocks.MAGENTA_TERRACOTTA).add(Blocks.MAGENTA_WALL_BANNER).add(Blocks.MAGENTA_WOOL);

		tag(ConventionalBlockTags.ORANGE_DYED)
				.add(Blocks.ORANGE_BANNER).add(Blocks.ORANGE_BED).add(Blocks.ORANGE_CANDLE).add(Blocks.ORANGE_CARPET)
				.add(Blocks.ORANGE_CONCRETE).add(Blocks.ORANGE_CONCRETE_POWDER).add(Blocks.ORANGE_GLAZED_TERRACOTTA)
				.add(Blocks.ORANGE_SHULKER_BOX).add(Blocks.ORANGE_STAINED_GLASS).add(Blocks.ORANGE_STAINED_GLASS_PANE)
				.add(Blocks.ORANGE_TERRACOTTA).add(Blocks.ORANGE_WALL_BANNER).add(Blocks.ORANGE_WOOL);

		tag(ConventionalBlockTags.PINK_DYED)
				.add(Blocks.PINK_BANNER).add(Blocks.PINK_BED).add(Blocks.PINK_CANDLE).add(Blocks.PINK_CARPET)
				.add(Blocks.PINK_CONCRETE).add(Blocks.PINK_CONCRETE_POWDER).add(Blocks.PINK_GLAZED_TERRACOTTA)
				.add(Blocks.PINK_SHULKER_BOX).add(Blocks.PINK_STAINED_GLASS).add(Blocks.PINK_STAINED_GLASS_PANE)
				.add(Blocks.PINK_TERRACOTTA).add(Blocks.PINK_WALL_BANNER).add(Blocks.PINK_WOOL);

		tag(ConventionalBlockTags.PURPLE_DYED)
				.add(Blocks.PURPLE_BANNER).add(Blocks.PURPLE_BED).add(Blocks.PURPLE_CANDLE).add(Blocks.PURPLE_CARPET)
				.add(Blocks.PURPLE_CONCRETE).add(Blocks.PURPLE_CONCRETE_POWDER).add(Blocks.PURPLE_GLAZED_TERRACOTTA)
				.add(Blocks.PURPLE_SHULKER_BOX).add(Blocks.PURPLE_STAINED_GLASS).add(Blocks.PURPLE_STAINED_GLASS_PANE)
				.add(Blocks.PURPLE_TERRACOTTA).add(Blocks.PURPLE_WALL_BANNER).add(Blocks.PURPLE_WOOL);

		tag(ConventionalBlockTags.RED_DYED)
				.add(Blocks.RED_BANNER).add(Blocks.RED_BED).add(Blocks.RED_CANDLE).add(Blocks.RED_CARPET)
				.add(Blocks.RED_CONCRETE).add(Blocks.RED_CONCRETE_POWDER).add(Blocks.RED_GLAZED_TERRACOTTA)
				.add(Blocks.RED_SHULKER_BOX).add(Blocks.RED_STAINED_GLASS).add(Blocks.RED_STAINED_GLASS_PANE)
				.add(Blocks.RED_TERRACOTTA).add(Blocks.RED_WALL_BANNER).add(Blocks.RED_WOOL);

		tag(ConventionalBlockTags.WHITE_DYED)
				.add(Blocks.WHITE_BANNER).add(Blocks.WHITE_BED).add(Blocks.WHITE_CANDLE).add(Blocks.WHITE_CARPET)
				.add(Blocks.WHITE_CONCRETE).add(Blocks.WHITE_CONCRETE_POWDER).add(Blocks.WHITE_GLAZED_TERRACOTTA)
				.add(Blocks.WHITE_SHULKER_BOX).add(Blocks.WHITE_STAINED_GLASS).add(Blocks.WHITE_STAINED_GLASS_PANE)
				.add(Blocks.WHITE_TERRACOTTA).add(Blocks.WHITE_WALL_BANNER).add(Blocks.WHITE_WOOL);

		tag(ConventionalBlockTags.YELLOW_DYED)
				.add(Blocks.YELLOW_BANNER).add(Blocks.YELLOW_BED).add(Blocks.YELLOW_CANDLE).add(Blocks.YELLOW_CARPET)
				.add(Blocks.YELLOW_CONCRETE).add(Blocks.YELLOW_CONCRETE_POWDER).add(Blocks.YELLOW_GLAZED_TERRACOTTA)
				.add(Blocks.YELLOW_SHULKER_BOX).add(Blocks.YELLOW_STAINED_GLASS).add(Blocks.YELLOW_STAINED_GLASS_PANE)
				.add(Blocks.YELLOW_TERRACOTTA).add(Blocks.YELLOW_WALL_BANNER).add(Blocks.YELLOW_WOOL);

		tag(ConventionalBlockTags.DYED)
				.addTag(ConventionalBlockTags.WHITE_DYED)
				.addTag(ConventionalBlockTags.ORANGE_DYED)
				.addTag(ConventionalBlockTags.MAGENTA_DYED)
				.addTag(ConventionalBlockTags.LIGHT_BLUE_DYED)
				.addTag(ConventionalBlockTags.YELLOW_DYED)
				.addTag(ConventionalBlockTags.LIME_DYED)
				.addTag(ConventionalBlockTags.PINK_DYED)
				.addTag(ConventionalBlockTags.GRAY_DYED)
				.addTag(ConventionalBlockTags.LIGHT_GRAY_DYED)
				.addTag(ConventionalBlockTags.CYAN_DYED)
				.addTag(ConventionalBlockTags.PURPLE_DYED)
				.addTag(ConventionalBlockTags.BLUE_DYED)
				.addTag(ConventionalBlockTags.BROWN_DYED)
				.addTag(ConventionalBlockTags.GREEN_DYED)
				.addTag(ConventionalBlockTags.RED_DYED)
				.addTag(ConventionalBlockTags.BLACK_DYED);
	}

	private void generateStorageTags() {
		tag(ConventionalBlockTags.STORAGE_BLOCKS)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_BONE_MEAL)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_COAL)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_COPPER)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_GOLD)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.addTag(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_BONE_MEAL)
				.add(Blocks.BONE_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_COAL)
				.add(Blocks.COAL_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_COPPER)
				.add(Blocks.COPPER_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_DIAMOND)
				.add(Blocks.DIAMOND_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_DRIED_KELP)
				.add(Blocks.DRIED_KELP_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_EMERALD)
				.add(Blocks.EMERALD_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_GOLD)
				.add(Blocks.GOLD_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
				.add(Blocks.IRON_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_LAPIS)
				.add(Blocks.LAPIS_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE)
				.add(Blocks.NETHERITE_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER)
				.add(Blocks.RAW_COPPER_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD)
				.add(Blocks.RAW_GOLD_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
				.add(Blocks.RAW_IRON_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_REDSTONE)
				.add(Blocks.REDSTONE_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_SLIME)
				.add(Blocks.SLIME_BLOCK);

		tag(ConventionalBlockTags.STORAGE_BLOCKS_WHEAT)
				.add(Blocks.HAY_BLOCK);
	}

	private void generateHeadTags() {
		tag(ConventionalBlockTags.SKULLS)
				.add(Blocks.SKELETON_SKULL)
				.add(Blocks.SKELETON_WALL_SKULL)
				.add(Blocks.WITHER_SKELETON_SKULL)
				.add(Blocks.WITHER_SKELETON_WALL_SKULL)
				.add(Blocks.PLAYER_HEAD)
				.add(Blocks.PLAYER_WALL_HEAD)
				.add(Blocks.ZOMBIE_HEAD)
				.add(Blocks.ZOMBIE_WALL_HEAD)
				.add(Blocks.CREEPER_HEAD)
				.add(Blocks.CREEPER_WALL_HEAD)
				.add(Blocks.PIGLIN_HEAD)
				.add(Blocks.PIGLIN_WALL_HEAD)
				.add(Blocks.DRAGON_HEAD)
				.add(Blocks.DRAGON_WALL_HEAD);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		tag(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "movement_restricted"));
		tag(ConventionalBlockTags.QUARTZ_ORES).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "quartz_ores"));
		tag(ConventionalBlockTags.WOODEN_BARRELS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "wooden_barrels"));
		tag(ConventionalBlockTags.WOODEN_CHESTS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "wooden_chests"));
		tag(ConventionalBlockTags.SANDSTONE_BLOCKS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "sandstone_blocks"));
		tag(ConventionalBlockTags.SANDSTONE_SLABS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "sandstone_slabs"));
		tag(ConventionalBlockTags.SANDSTONE_STAIRS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "sandstone_stairs"));
		tag(ConventionalBlockTags.RED_SANDSTONE_BLOCKS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "red_sandstone_blocks"));
		tag(ConventionalBlockTags.RED_SANDSTONE_SLABS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "red_sandstone_slabs"));
		tag(ConventionalBlockTags.RED_SANDSTONE_STAIRS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "red_sandstone_stairs"));
		tag(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_blocks"));
		tag(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_slabs"));
		tag(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "uncolored_sandstone_stairs"));
		tag(ConventionalBlockTags.GLAZED_TERRACOTTAS).addOptionalTag(ConventionalBlockTags.GLAZED_TERRACOTTA);
		tag(ConventionalBlockTags.CONCRETES).addOptionalTag(ConventionalBlockTags.CONCRETE);
	}
}

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

import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.unmapped.C_rciogtqy;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.TagDataProvider;
import com.bookkeepersmc.notebook.api.tag.common.v1.ConventionalBiomeTags;
import com.bookkeepersmc.notebook.api.tag.common.v1.TagUtil;

public final class BiomeTagGenerator extends TagDataProvider<Biome> {
	public BiomeTagGenerator(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, Registries.BIOME, registriesFuture);
	}

	@Override
	protected void configure(HolderLookup.Provider wrapperLookup) {
		generateDimensionTags();
		generateCategoryTags();
		generateOtherBiomeTypes();
		generateClimateAndVegetationTags();
		generateTerrainDescriptorTags();
		generateBackwardsCompatTags();
	}

	private void generateDimensionTags() {
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_NETHER)
				.addOptionalTag(BiomeTags.NETHER);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_END)
				.addOptionalTag(BiomeTags.END);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_OVERWORLD)
				.addOptionalTag(BiomeTags.OVERWORLD);
	}

	private void generateCategoryTags() {
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_TAIGA)
				.addOptionalTag(BiomeTags.TAIGA);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_HILL)
				.addOptionalTag(BiomeTags.HILL);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_WINDSWEPT)
				.add(Biomes.WINDSWEPT_HILLS)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.WINDSWEPT_SAVANNA);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_JUNGLE)
				.addOptionalTag(BiomeTags.JUNGLE);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_PLAINS)
				.add(Biomes.PLAINS)
				.add(Biomes.SUNFLOWER_PLAINS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_SAVANNA)
				.addOptionalTag(BiomeTags.SAVANNA);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_ICY)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.ICE_SPIKES);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_AQUATIC_ICY)
				.add(Biomes.FROZEN_RIVER)
				.add(Biomes.DEEP_FROZEN_OCEAN)
				.add(Biomes.FROZEN_OCEAN);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_SNOWY)
				.add(Biomes.SNOWY_BEACH)
				.add(Biomes.SNOWY_PLAINS)
				.add(Biomes.ICE_SPIKES)
				.add(Biomes.SNOWY_TAIGA)
				.add(Biomes.GROVE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.FROZEN_PEAKS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_BEACH)
				.addOptionalTag(BiomeTags.BEACH);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_FOREST)
				.addOptionalTag(BiomeTags.FOREST);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_BIRCH_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DARK_FOREST)
				.add(Biomes.DARK_FOREST)
				.addOptional(C_rciogtqy.PALE_GARDEN);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(BiomeTags.OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_SHALLOW_OCEAN);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DESERT)
				.add(Biomes.DESERT);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_RIVER)
				.addOptionalTag(BiomeTags.RIVER);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.SWAMP);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_MUSHROOM)
				.add(Biomes.MUSHROOM_FIELDS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_UNDERGROUND)
				.addOptionalTag(ConventionalBiomeTags.IS_CAVE);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_MOUNTAIN)
				.addOptionalTag(BiomeTags.MOUNTAIN)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE);
	}

	private void generateOtherBiomeTypes() {
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_BADLANDS)
				.addOptionalTag(BiomeTags.BADLANDS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_CAVE)
				.add(Biomes.DEEP_DARK)
				.add(Biomes.DRIPSTONE_CAVES)
				.add(Biomes.LUSH_CAVES);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_VOID)
				.add(Biomes.THE_VOID);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(BiomeTags.DEEP_OCEAN);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_SHALLOW_OCEAN)
				.add(Biomes.OCEAN)
				.add(Biomes.LUKEWARM_OCEAN)
				.add(Biomes.WARM_OCEAN)
				.add(Biomes.COLD_OCEAN)
				.add(Biomes.FROZEN_OCEAN);
		getOrCreateTagBuilder(ConventionalBiomeTags.NO_DEFAULT_MONSTERS)
				.add(Biomes.MUSHROOM_FIELDS)
				.add(Biomes.DEEP_DARK);
		getOrCreateTagBuilder(ConventionalBiomeTags.HIDDEN_FROM_LOCATOR_SELECTION); // Create tag file for visibility
	}

	private void generateClimateAndVegetationTags() {
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_COLD_OVERWORLD)
				.add(Biomes.TAIGA)
				.add(Biomes.OLD_GROWTH_PINE_TAIGA)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.add(Biomes.WINDSWEPT_HILLS)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.SNOWY_PLAINS)
				.add(Biomes.ICE_SPIKES)
				.add(Biomes.GROVE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.STONY_SHORE)
				.add(Biomes.SNOWY_BEACH)
				.add(Biomes.SNOWY_TAIGA)
				.add(Biomes.FROZEN_RIVER)
				.add(Biomes.COLD_OCEAN)
				.add(Biomes.FROZEN_OCEAN)
				.add(Biomes.DEEP_COLD_OCEAN)
				.add(Biomes.DEEP_FROZEN_OCEAN);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_COLD_END)
				.add(Biomes.THE_END)
				.add(Biomes.SMALL_END_ISLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_BARRENS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_COLD)
				.addTag(ConventionalBiomeTags.IS_COLD_OVERWORLD);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD)
				.add(Biomes.FOREST)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.SWAMP)
				.add(Biomes.STONY_SHORE)
				.add(Biomes.DARK_FOREST)
				.addOptional(C_rciogtqy.PALE_GARDEN)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.MEADOW)
				.add(Biomes.PLAINS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_TEMPERATE)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_HOT_OVERWORLD)
				.add(Biomes.SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.DESERT)
				.add(Biomes.BADLANDS)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.ERODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA)
				.add(Biomes.STONY_PEAKS)
				.add(Biomes.WARM_OCEAN);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_HOT_NETHER)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.CRIMSON_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.BASALT_DELTAS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_HOT)
				.addTag(ConventionalBiomeTags.IS_HOT_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_HOT_NETHER);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_WET_OVERWORLD)
				.add(Biomes.SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.BEACH)
				.add(Biomes.LUSH_CAVES)
				.add(Biomes.DRIPSTONE_CAVES);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_WET)
				.addTag(ConventionalBiomeTags.IS_WET_OVERWORLD);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.add(Biomes.DESERT)
				.add(Biomes.BADLANDS)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.ERODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DRY_NETHER)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.CRIMSON_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.BASALT_DELTAS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DRY_END)
				.add(Biomes.THE_END)
				.add(Biomes.SMALL_END_ISLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_BARRENS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DRY)
				.addTag(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_DRY_NETHER)
				.addTag(ConventionalBiomeTags.IS_DRY_END);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD)
				.add(Biomes.DARK_FOREST)
				.addOptional(C_rciogtqy.PALE_GARDEN)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.MANGROVE_SWAMP);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_VEGETATION_DENSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.WINDSWEPT_SAVANNA)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.WINDSWEPT_HILLS)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.FROZEN_PEAKS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_VEGETATION_SPARSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_CONIFEROUS_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_TAIGA)
				.add(Biomes.GROVE);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DECIDUOUS_TREE)
				.add(Biomes.FOREST)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.FLOWER_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.DARK_FOREST)
				.addOptional(C_rciogtqy.PALE_GARDEN)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_JUNGLE_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_JUNGLE);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_SAVANNA_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_SAVANNA);

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_FLORAL)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.MEADOW)
				.add(Biomes.CHERRY_GROVE)
				.addOptionalTag(ConventionalBiomeTags.IS_FLOWER_FOREST);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_FLOWER_FOREST)
				.add(Biomes.FLOWER_FOREST)
				.addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "flower_forests"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_OLD_GROWTH)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_PINE_TAIGA)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
	}

	private void generateTerrainDescriptorTags() {
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.STONY_PEAKS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.MEADOW)
				.add(Biomes.GROVE)
				.add(Biomes.CHERRY_GROVE);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_AQUATIC)
				.addOptionalTag(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_RIVER);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DEAD);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_WASTELAND);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_OUTER_END_ISLAND)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_BARRENS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_NETHER_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.CRIMSON_FOREST);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_SNOWY_PLAINS)
				.add(Biomes.SNOWY_PLAINS);
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_STONY_SHORES)
				.add(Biomes.STONY_SHORE);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		getOrCreateTagBuilder(ConventionalBiomeTags.IS_NETHER).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "in_nether"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_END).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "in_the_end"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "in_the_overworld"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_CAVE).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "caves"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_COLD_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_cold"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_temperate"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_HOT_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_hot"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_WET_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_wet"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DRY_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "climate_dry"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "vegetation_dense"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "vegetation_sparse"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_CONIFEROUS_TREE).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_coniferous"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_DECIDUOUS_TREE).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_deciduous"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_JUNGLE_TREE).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_jungle"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_SAVANNA_TREE).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "tree_savanna"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_MOUNTAIN_PEAK).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "mountain_peak"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "mountain_slope"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_OUTER_END_ISLAND).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "end_islands"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_NETHER_FOREST).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "nether_forests"));
		getOrCreateTagBuilder(ConventionalBiomeTags.IS_FLOWER_FOREST).addOptionalTag(Identifier.of(TagUtil.C_TAG_NAMESPACE, "flower_forests"));
	}
}

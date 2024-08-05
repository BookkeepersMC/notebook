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

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.TagDataProvider;
import com.bookkeepersmc.notebook.api.tag.common.v1.ConventionalBiomeTags;
import com.bookkeepersmc.notebook.api.tag.common.v1.TagUtil;

public final class BiomeTagGenerator extends TagDataProvider<Biome> {
	public BiomeTagGenerator(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, Registries.BIOME, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		generateDimensionTags();
		generateCategoryTags();
		generateOtherBiomeTypes();
		generateClimateAndVegetationTags();
		generateTerrainDescriptorTags();
		generateBackwardsCompatTags();
	}

	private void generateDimensionTags() {
		tag(ConventionalBiomeTags.IS_NETHER)
				.addOptionalTag(BiomeTags.IS_NETHER);
		tag(ConventionalBiomeTags.IS_END)
				.addOptionalTag(BiomeTags.IS_END);
		tag(ConventionalBiomeTags.IS_OVERWORLD)
				.addOptionalTag(BiomeTags.IS_OVERWORLD);
	}

	private void generateCategoryTags() {
		tag(ConventionalBiomeTags.IS_TAIGA)
				.addOptionalTag(BiomeTags.IS_TAIGA);
		tag(ConventionalBiomeTags.IS_HILL)
				.addOptionalTag(BiomeTags.IS_HILL);
		tag(ConventionalBiomeTags.IS_WINDSWEPT)
				.add(Biomes.WINDSWEPT_HILLS)
				.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.WINDSWEPT_SAVANNA);
		tag(ConventionalBiomeTags.IS_JUNGLE)
				.addOptionalTag(BiomeTags.IS_JUNGLE);
		tag(ConventionalBiomeTags.IS_PLAINS)
				.add(Biomes.PLAINS)
				.add(Biomes.SUNFLOWER_PLAINS);
		tag(ConventionalBiomeTags.IS_SAVANNA)
				.addOptionalTag(BiomeTags.IS_SAVANNA);
		tag(ConventionalBiomeTags.IS_ICY)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.ICE_SPIKES);
		tag(ConventionalBiomeTags.IS_AQUATIC_ICY)
				.add(Biomes.FROZEN_RIVER)
				.add(Biomes.DEEP_FROZEN_OCEAN)
				.add(Biomes.FROZEN_OCEAN);
		tag(ConventionalBiomeTags.IS_SNOWY)
				.add(Biomes.SNOWY_BEACH)
				.add(Biomes.SNOWY_PLAINS)
				.add(Biomes.ICE_SPIKES)
				.add(Biomes.SNOWY_TAIGA)
				.add(Biomes.GROVE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.FROZEN_PEAKS);
		tag(ConventionalBiomeTags.IS_BEACH)
				.addOptionalTag(BiomeTags.IS_BEACH);
		tag(ConventionalBiomeTags.IS_FOREST)
				.addOptionalTag(BiomeTags.IS_FOREST);
		tag(ConventionalBiomeTags.IS_BIRCH_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST);
		tag(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(BiomeTags.IS_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_SHALLOW_OCEAN);
		tag(ConventionalBiomeTags.IS_DESERT)
				.add(Biomes.DESERT);
		tag(ConventionalBiomeTags.IS_RIVER)
				.addOptionalTag(BiomeTags.IS_RIVER);
		tag(ConventionalBiomeTags.IS_SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.SWAMP);
		tag(ConventionalBiomeTags.IS_MUSHROOM)
				.add(Biomes.MUSHROOM_FIELDS);
		tag(ConventionalBiomeTags.IS_UNDERGROUND)
				.addOptionalTag(ConventionalBiomeTags.IS_CAVE);
		tag(ConventionalBiomeTags.IS_MOUNTAIN)
				.addOptionalTag(BiomeTags.IS_MOUNTAIN)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE);
	}

	private void generateOtherBiomeTypes() {
		tag(ConventionalBiomeTags.IS_BADLANDS)
				.addOptionalTag(BiomeTags.IS_BADLANDS);
		tag(ConventionalBiomeTags.IS_CAVE)
				.add(Biomes.DEEP_DARK)
				.add(Biomes.DRIPSTONE_CAVES)
				.add(Biomes.LUSH_CAVES);
		tag(ConventionalBiomeTags.IS_VOID)
				.add(Biomes.THE_VOID);
		tag(ConventionalBiomeTags.IS_DEEP_OCEAN)
				.addOptionalTag(BiomeTags.IS_DEEP_OCEAN);
		tag(ConventionalBiomeTags.IS_SHALLOW_OCEAN)
				.add(Biomes.OCEAN)
				.add(Biomes.LUKEWARM_OCEAN)
				.add(Biomes.WARM_OCEAN)
				.add(Biomes.COLD_OCEAN)
				.add(Biomes.FROZEN_OCEAN);
		tag(ConventionalBiomeTags.NO_DEFAULT_MONSTERS)
				.add(Biomes.MUSHROOM_FIELDS)
				.add(Biomes.DEEP_DARK);
		tag(ConventionalBiomeTags.HIDDEN_FROM_LOCATOR_SELECTION); // Create tag file for visibility
	}

	private void generateClimateAndVegetationTags() {
		tag(ConventionalBiomeTags.IS_COLD_OVERWORLD)
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
		tag(ConventionalBiomeTags.IS_COLD_END)
				.add(Biomes.THE_END)
				.add(Biomes.SMALL_END_ISLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_BARRENS);
		tag(ConventionalBiomeTags.IS_COLD)
				.addTag(ConventionalBiomeTags.IS_COLD_OVERWORLD);

		tag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD)
				.add(Biomes.FOREST)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.SWAMP)
				.add(Biomes.STONY_SHORE)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.MEADOW)
				.add(Biomes.PLAINS);
		tag(ConventionalBiomeTags.IS_TEMPERATE)
				.addTag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD);

		tag(ConventionalBiomeTags.IS_HOT_OVERWORLD)
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
		tag(ConventionalBiomeTags.IS_HOT_NETHER)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.CRIMSON_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.BASALT_DELTAS);
		tag(ConventionalBiomeTags.IS_HOT)
				.addTag(ConventionalBiomeTags.IS_HOT_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_HOT_NETHER);

		tag(ConventionalBiomeTags.IS_WET_OVERWORLD)
				.add(Biomes.SWAMP)
				.add(Biomes.MANGROVE_SWAMP)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.SPARSE_JUNGLE)
				.add(Biomes.BEACH)
				.add(Biomes.LUSH_CAVES)
				.add(Biomes.DRIPSTONE_CAVES);
		tag(ConventionalBiomeTags.IS_WET)
				.addTag(ConventionalBiomeTags.IS_WET_OVERWORLD);

		tag(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.add(Biomes.DESERT)
				.add(Biomes.BADLANDS)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.ERODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA);
		tag(ConventionalBiomeTags.IS_DRY_NETHER)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.CRIMSON_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.BASALT_DELTAS);
		tag(ConventionalBiomeTags.IS_DRY_END)
				.add(Biomes.THE_END)
				.add(Biomes.SMALL_END_ISLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_BARRENS);
		tag(ConventionalBiomeTags.IS_DRY)
				.addTag(ConventionalBiomeTags.IS_DRY_OVERWORLD)
				.addTag(ConventionalBiomeTags.IS_DRY_NETHER)
				.addTag(ConventionalBiomeTags.IS_DRY_END);

		tag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
				.add(Biomes.JUNGLE)
				.add(Biomes.BAMBOO_JUNGLE)
				.add(Biomes.MANGROVE_SWAMP);
		tag(ConventionalBiomeTags.IS_VEGETATION_DENSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD);

		tag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD)
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
		tag(ConventionalBiomeTags.IS_VEGETATION_SPARSE)
				.addOptionalTag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD);

		tag(ConventionalBiomeTags.IS_CONIFEROUS_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_TAIGA)
				.add(Biomes.GROVE);
		tag(ConventionalBiomeTags.IS_DECIDUOUS_TREE)
				.add(Biomes.FOREST)
				.add(Biomes.WINDSWEPT_FOREST)
				.add(Biomes.FLOWER_FOREST)
				.add(Biomes.BIRCH_FOREST)
				.add(Biomes.DARK_FOREST)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST);
		tag(ConventionalBiomeTags.IS_JUNGLE_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_JUNGLE);
		tag(ConventionalBiomeTags.IS_SAVANNA_TREE)
				.addOptionalTag(ConventionalBiomeTags.IS_SAVANNA);

		tag(ConventionalBiomeTags.IS_FLORAL)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.MEADOW)
				.add(Biomes.CHERRY_GROVE)
				.addOptionalTag(ConventionalBiomeTags.IS_FLOWER_FOREST);
		tag(ConventionalBiomeTags.IS_FLOWER_FOREST)
				.add(Biomes.FLOWER_FOREST)
				.addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "flower_forests"));
		tag(ConventionalBiomeTags.IS_OLD_GROWTH)
				.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
				.add(Biomes.OLD_GROWTH_PINE_TAIGA)
				.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
	}

	private void generateTerrainDescriptorTags() {
		tag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
				.add(Biomes.FROZEN_PEAKS)
				.add(Biomes.JAGGED_PEAKS)
				.add(Biomes.STONY_PEAKS);
		tag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE)
				.add(Biomes.SNOWY_SLOPES)
				.add(Biomes.MEADOW)
				.add(Biomes.GROVE)
				.add(Biomes.CHERRY_GROVE);
		tag(ConventionalBiomeTags.IS_AQUATIC)
				.addOptionalTag(ConventionalBiomeTags.IS_OCEAN)
				.addOptionalTag(ConventionalBiomeTags.IS_RIVER);
		tag(ConventionalBiomeTags.IS_DEAD);
		tag(ConventionalBiomeTags.IS_WASTELAND);
		tag(ConventionalBiomeTags.IS_OUTER_END_ISLAND)
				.add(Biomes.END_HIGHLANDS)
				.add(Biomes.END_MIDLANDS)
				.add(Biomes.END_BARRENS);
		tag(ConventionalBiomeTags.IS_NETHER_FOREST)
				.add(Biomes.WARPED_FOREST)
				.add(Biomes.CRIMSON_FOREST);
		tag(ConventionalBiomeTags.IS_SNOWY_PLAINS)
				.add(Biomes.SNOWY_PLAINS);
		tag(ConventionalBiomeTags.IS_STONY_SHORES)
				.add(Biomes.STONY_SHORE);
	}

	private void generateBackwardsCompatTags() {
		// Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
		// TODO: Remove backwards compat tag entries in 1.22

		tag(ConventionalBiomeTags.IS_NETHER).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "in_nether"));
		tag(ConventionalBiomeTags.IS_END).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "in_the_end"));
		tag(ConventionalBiomeTags.IS_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "in_the_overworld"));
		tag(ConventionalBiomeTags.IS_CAVE).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "caves"));
		tag(ConventionalBiomeTags.IS_COLD_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_cold"));
		tag(ConventionalBiomeTags.IS_TEMPERATE_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_temperate"));
		tag(ConventionalBiomeTags.IS_HOT_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_hot"));
		tag(ConventionalBiomeTags.IS_WET_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_wet"));
		tag(ConventionalBiomeTags.IS_DRY_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "climate_dry"));
		tag(ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "vegetation_dense"));
		tag(ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "vegetation_sparse"));
		tag(ConventionalBiomeTags.IS_CONIFEROUS_TREE).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_coniferous"));
		tag(ConventionalBiomeTags.IS_DECIDUOUS_TREE).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_deciduous"));
		tag(ConventionalBiomeTags.IS_JUNGLE_TREE).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_jungle"));
		tag(ConventionalBiomeTags.IS_SAVANNA_TREE).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "tree_savanna"));
		tag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "mountain_peak"));
		tag(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "mountain_slope"));
		tag(ConventionalBiomeTags.IS_OUTER_END_ISLAND).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "end_islands"));
		tag(ConventionalBiomeTags.IS_NETHER_FOREST).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "nether_forests"));
		tag(ConventionalBiomeTags.IS_FLOWER_FOREST).addOptionalTag(ResourceLocation.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, "flower_forests"));
	}
}

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
package com.bookkeepersmc.notebook.api.tag.common.v1;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

import com.bookkeepersmc.notebook.impl.tag.common.v1.TagRegistration;

// Copied from FabricMC, as these tags are supposed to work on Fabric-Neoforge-Notebook
public class ConventionalBiomeTags {
	private ConventionalBiomeTags() {
	}

	public static final TagKey<Biome> NO_DEFAULT_MONSTERS = register("no_default_monsters");
	/**
	 * Biomes that should not be locatable/selectable by modded biome-locating items or abilities.
	 */
	public static final TagKey<Biome> HIDDEN_FROM_LOCATOR_SELECTION = register("hidden_from_locator_selection");

	public static final TagKey<Biome> IS_VOID = register("is_void");

	/**
	 * Biomes that spawn in the Overworld.
	 * (This is for people who want to tag their biomes as Overworld without getting
	 * side effects from {@link net.minecraft.tags.BiomeTags#IS_OVERWORLD}.
	 * <p></p>
	 * NOTE: If you do not add to the vanilla Overworld tag, be sure to add to
	 * {@link net.minecraft.tags.BiomeTags#HAS_STRONGHOLD} so
	 * some Strongholds do not go missing.)
	 */
	public static final TagKey<Biome> IS_OVERWORLD = register("is_overworld");

	public static final TagKey<Biome> IS_HOT = register("is_hot");
	public static final TagKey<Biome> IS_HOT_OVERWORLD = register("is_hot/overworld");
	public static final TagKey<Biome> IS_HOT_NETHER = register("is_hot/nether");

	public static final TagKey<Biome> IS_TEMPERATE = register("is_temperate");
	public static final TagKey<Biome> IS_TEMPERATE_OVERWORLD = register("is_temperate/overworld");

	public static final TagKey<Biome> IS_COLD = register("is_cold");
	public static final TagKey<Biome> IS_COLD_OVERWORLD = register("is_cold/overworld");
	public static final TagKey<Biome> IS_COLD_END = register("is_cold/end");

	public static final TagKey<Biome> IS_WET = register("is_wet");
	public static final TagKey<Biome> IS_WET_OVERWORLD = register("is_wet/overworld");

	public static final TagKey<Biome> IS_DRY = register("is_dry");
	public static final TagKey<Biome> IS_DRY_OVERWORLD = register("is_dry/overworld");
	public static final TagKey<Biome> IS_DRY_NETHER = register("is_dry/nether");
	public static final TagKey<Biome> IS_DRY_END = register("is_dry/end");

	public static final TagKey<Biome> IS_VEGETATION_SPARSE = register("is_sparse_vegetation");
	public static final TagKey<Biome> IS_VEGETATION_SPARSE_OVERWORLD = register("is_sparse_vegetation/overworld");

	public static final TagKey<Biome> IS_VEGETATION_DENSE = register("is_dense_vegetation");
	public static final TagKey<Biome> IS_VEGETATION_DENSE_OVERWORLD = register("is_dense_vegetation/overworld");

	public static final TagKey<Biome> IS_CONIFEROUS_TREE = register("is_tree/coniferous");
	public static final TagKey<Biome> IS_SAVANNA_TREE = register("is_tree/savanna");
	public static final TagKey<Biome> IS_JUNGLE_TREE = register("is_tree/jungle");
	public static final TagKey<Biome> IS_DECIDUOUS_TREE = register("is_tree/deciduous");

	public static final TagKey<Biome> IS_MOUNTAIN = register("is_mountain");
	public static final TagKey<Biome> IS_MOUNTAIN_PEAK = register("is_mountain/peak");
	public static final TagKey<Biome> IS_MOUNTAIN_SLOPE = register("is_mountain/slope");

	/**
	 * For temperate or warmer plains-like biomes.
	 * For snowy plains-like biomes, see {@link ConventionalBiomeTags#IS_SNOWY_PLAINS}.
	 */
	public static final TagKey<Biome> IS_PLAINS = register("is_plains");
	/**
	 * For snowy plains-like biomes.
	 * For warmer plains-like biomes, see {@link ConventionalBiomeTags#IS_PLAINS}.
	 */
	public static final TagKey<Biome> IS_SNOWY_PLAINS = register("is_snowy_plains");
	/**
	 * Biomes densely populated with deciduous trees.
	 */
	public static final TagKey<Biome> IS_FOREST = register("is_forest");
	public static final TagKey<Biome> IS_BIRCH_FOREST = register("is_birch_forest");
	public static final TagKey<Biome> IS_FLOWER_FOREST = register("is_flower_forest");
	public static final TagKey<Biome> IS_TAIGA = register("is_taiga");
	public static final TagKey<Biome> IS_OLD_GROWTH = register("is_old_growth");
	/**
	 * Biomes that spawn as a hills biome. (Previously was called Extreme Hills biome in past)
	 */
	public static final TagKey<Biome> IS_HILL = register("is_hill");
	public static final TagKey<Biome> IS_WINDSWEPT = register("is_windswept");
	public static final TagKey<Biome> IS_JUNGLE = register("is_jungle");
	public static final TagKey<Biome> IS_SAVANNA = register("is_savanna");
	public static final TagKey<Biome> IS_SWAMP = register("is_swamp");
	public static final TagKey<Biome> IS_DESERT = register("is_desert");
	public static final TagKey<Biome> IS_BADLANDS = register("is_badlands");
	/**
	 * Biomes that are dedicated to spawning on the shoreline of a body of water.
	 */
	public static final TagKey<Biome> IS_BEACH = register("is_beach");
	public static final TagKey<Biome> IS_STONY_SHORES = register("is_stony_shores");
	public static final TagKey<Biome> IS_MUSHROOM = register("is_mushroom");

	public static final TagKey<Biome> IS_RIVER = register("is_river");
	public static final TagKey<Biome> IS_OCEAN = register("is_ocean");
	public static final TagKey<Biome> IS_DEEP_OCEAN = register("is_deep_ocean");
	public static final TagKey<Biome> IS_SHALLOW_OCEAN = register("is_shallow_ocean");

	public static final TagKey<Biome> IS_UNDERGROUND = register("is_underground");
	public static final TagKey<Biome> IS_CAVE = register("is_cave");

	/**
	 * Biomes that lack any natural life or vegetation.
	 * (Example, land destroyed and sterilized by nuclear weapons)
	 */
	public static final TagKey<Biome> IS_WASTELAND = register("is_wasteland");
	/**
	 * Biomes whose flora primarily consists of dead or decaying vegetation.
	 */
	public static final TagKey<Biome> IS_DEAD = register("is_dead");
	/**
	 * Biomes with a large amount of flowers.
	 */
	public static final TagKey<Biome> IS_FLORAL = register("is_floral");
	/**
	 * For biomes that contains lots of naturally spawned snow.
	 * For biomes where lot of ice is present, see {@link ConventionalBiomeTags#IS_ICY}.
	 * Biome with lots of both snow and ice may be in both tags.
	 */
	public static final TagKey<Biome> IS_SNOWY = register("is_snowy");
	/**
	 * For land biomes where ice naturally spawns.
	 * For biomes where snow alone spawns, see {@link ConventionalBiomeTags#IS_SNOWY}.
	 */
	public static final TagKey<Biome> IS_ICY = register("is_icy");
	/**
	 * Biomes consisting primarily of water.
	 */
	public static final TagKey<Biome> IS_AQUATIC = register("is_aquatic");
	/**
	 * For water biomes where ice naturally spawns.
	 * For biomes where snow alone spawns, see {@link ConventionalBiomeTags#IS_SNOWY}.
	 */
	public static final TagKey<Biome> IS_AQUATIC_ICY = register("is_aquatic_icy");

	/**
	 * Biomes that spawn in the Nether.
	 * (This is for people who want to tag their biomes as Nether without getting
	 * side effects from {@link net.minecraft.tags.BiomeTags#IS_NETHER})
	 */
	public static final TagKey<Biome> IS_NETHER = register("is_nether");
	public static final TagKey<Biome> IS_NETHER_FOREST = register("is_nether_forest");

	/**
	 * Biomes that spawn in the End.
	 * (This is for people who want to tag their biomes as End without getting
	 * side effects from {@link net.minecraft.tags.BiomeTags#IS_END})
	 */
	public static final TagKey<Biome> IS_END = register("is_end");
	/**
	 * Biomes that spawn as part of the large islands outside the center island in The End dimension.
	 */
	public static final TagKey<Biome> IS_OUTER_END_ISLAND = register("is_outer_end_island");

	private static TagKey<Biome> register(String tagId) {
		return TagRegistration.BIOME_TAG.registerC(tagId);
	}
}

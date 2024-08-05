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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.impl.datagen.NotebookDatagenHelper;

public abstract class RecipeDataProvider extends RecipeProvider {
	protected final NotebookDataOutput output;

	public RecipeDataProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
		this.output = output;
	}

	@Override
	public abstract void buildRecipes(RecipeOutput exporter);

	protected RecipeOutput withConditions(RecipeOutput exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return new RecipeOutput() {
			@Override
			public void accept(ResourceLocation identifier, Recipe<?> recipe, @Nullable AdvancementHolder advancementEntry) {
				NotebookDatagenHelper.addConditions(recipe, conditions);
				exporter.accept(identifier, recipe, advancementEntry);
			}

			@Override
			public Advancement.Builder advancement() {
				return exporter.advancement();
			}
		};
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer, HolderLookup.Provider wrapperLookup) {
		Set<ResourceLocation> generatedRecipes = Sets.newHashSet();
		List<CompletableFuture<?>> list = new ArrayList<>();
		buildRecipes(new RecipeOutput() {
			@Override
			public void accept(ResourceLocation recipeId, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
				ResourceLocation identifier = getRecipeIdentifier(recipeId);

				if (!generatedRecipes.add(identifier)) {
					throw new IllegalStateException("Duplicate recipe " + identifier);
				}

				RegistryOps<JsonElement> registryOps = wrapperLookup.createSerializationContext(JsonOps.INSTANCE);
				JsonObject recipeJson = Recipe.CODEC.encodeStart(registryOps, recipe).getOrThrow(IllegalStateException::new).getAsJsonObject();
				ResourceCondition[] conditions = NotebookDatagenHelper.consumeConditions(recipe);
				NotebookDatagenHelper.addConditions(recipeJson, conditions);

				list.add(DataProvider.saveStable(writer, recipeJson, recipePathProvider.json(identifier)));

				if (advancement != null) {
					JsonObject advancementJson = Advancement.CODEC.encodeStart(registryOps, advancement.value()).getOrThrow(IllegalStateException::new).getAsJsonObject();
					NotebookDatagenHelper.addConditions(advancementJson, conditions);
					list.add(DataProvider.saveStable(writer, advancementJson, advancementPathProvider.json(getRecipeIdentifier(advancement.id()))));
				}
			}

			@Override
			public Advancement.Builder advancement() {
				//noinspection removal
				return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
			}
		});
		return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
	}

	/**
	 * Override this method to change the recipe identifier. The default implementation normalizes the namespace to the mod ID.
	 */
	protected ResourceLocation getRecipeIdentifier(ResourceLocation identifier) {
		return ResourceLocation.fromNamespaceAndPath(output.getModId(), identifier.getPath());
	}
}

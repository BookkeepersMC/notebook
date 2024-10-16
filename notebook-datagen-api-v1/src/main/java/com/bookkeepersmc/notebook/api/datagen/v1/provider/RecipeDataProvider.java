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

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementHolder;
import net.minecraft.data.DataPackOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.server.RecipesProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeJsonFactory;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ResourceKey;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.resource.conditions.v1.ResourceCondition;
import com.bookkeepersmc.notebook.impl.datagen.NotebookDatagenHelper;

public abstract class RecipeDataProvider extends RecipesProvider.C_ujfsvkmt {
	protected final NotebookDataOutput output;
	private final CompletableFuture<HolderLookup.Provider> provider;

	public RecipeDataProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
		this.output = output;
		this.provider = registriesFuture;
	}

	@Override
	public abstract RecipesProvider method_62766(HolderLookup.Provider provider, RecipeExporter exporter);

	protected RecipeExporter withConditions(RecipeExporter exporter, ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
		return new RecipeExporter() {
			@Override
			public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancementEntry) {
				NotebookDatagenHelper.addConditions(recipe, conditions);
				exporter.accept(key, recipe, advancementEntry);
			}

			@Override
			public Advancement.Builder accept() {
				return exporter.accept()	;
			}

			@Override
			public void method_62738() {

			}
		};
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return provider.thenCompose((wrapperLookup -> {
			Set<Identifier> generatedRecipes = Sets.newHashSet();
			List<CompletableFuture<?>> list = new ArrayList<>();
			RecipesProvider recipesProvider =  method_62766(wrapperLookup, new RecipeExporter() {
				@Override
				public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
					Identifier identifier = getRecipeIdentifier(key.getValue());

					if (!generatedRecipes.add(identifier)) {
						throw new IllegalStateException("Duplicate recipe " + identifier);
					}

					RegistryOps<JsonElement> registryOps = wrapperLookup.createSerializationContext(JsonOps.INSTANCE);
					JsonObject recipeJson = Recipe.CODEC.encodeStart(registryOps, recipe).getOrThrow(IllegalStateException::new).getAsJsonObject();
					ResourceCondition[] conditions = NotebookDatagenHelper.consumeConditions(recipe);
					NotebookDatagenHelper.addConditions(recipeJson, conditions);

					final DataPackOutput.PathResolver recipesPathResolver = output.createPathResolver(Registries.RECIPE);
					final DataPackOutput.PathResolver advancementsPathResolver = output.createPathResolver(Registries.ADVANCEMENT);

					list.add(DataProvider.writeToPath(writer, recipeJson, recipesPathResolver.resolveJsonFile(identifier)));

					if (advancement != null) {
						JsonObject advancementJson = Advancement.CODEC.encodeStart(registryOps, advancement.data()).getOrThrow(IllegalStateException::new).getAsJsonObject();
						NotebookDatagenHelper.addConditions(advancementJson, conditions);
						list.add(DataProvider.writeToPath(writer, advancementJson, advancementsPathResolver.resolveJsonFile(getRecipeIdentifier(advancement.id()))));
					}
				}

				@Override
				@SuppressWarnings("removal")
				public Advancement.Builder accept() {
					return Advancement.Builder.create().parent(RecipeJsonFactory.ROOT_ID);
				}

				@Override
				public void method_62738() {
				}
			});
			recipesProvider.generateRecipes();
			return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
		}));
	}

	/**
	 * Override this method to change the recipe identifier. The default implementation normalizes the namespace to the mod ID.
	 */
	protected Identifier getRecipeIdentifier(Identifier identifier) {
		return Identifier.of(output.getModId(), identifier.getPath());
	}
}

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
package com.bookkeepersmc.notebook.mixin.datagen;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.ModelDataProvider;

@Mixin(ModelProvider.class)
public class ModelProviderMixin {
	@Unique
	private NotebookDataOutput notebookDataOutput;

	@Unique
	private static final ThreadLocal<NotebookDataOutput> notebookDataOutputThreadLocal = new ThreadLocal<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(PackOutput output, CallbackInfo info) {
		if (output instanceof NotebookDataOutput notebookDataOutput) {
			this.notebookDataOutput = notebookDataOutput;
		}
	}

	@Unique
	private static ThreadLocal<Map<Block, BlockStateGenerator>> blockStateMapThreadLocal = new ThreadLocal<>();

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/models/BlockModelGenerators;run()V"))
	private void registerBlockStateModels(BlockModelGenerators instance) {
		if (((Object) this) instanceof ModelDataProvider modelDataProvider) {
			modelDataProvider.generateBlockStateModels(instance);
		} else {
			instance.run();
		}
	}

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/models/ItemModelGenerators;run()V"))
	private void registerItemModels(ItemModelGenerators instance) {
		if (((Object) this) instanceof ModelDataProvider modelDataProvider) {
			modelDataProvider.generateItemModels(instance);
		} else {
			instance.run();
		}
	}

	@Inject(method = "run", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", ordinal = 0, remap = false))
	private void runHead(CachedOutput cachedOutput, CallbackInfoReturnable<CompletableFuture<?>> cir, @Local Map<Block, BlockStateGenerator> map) {
		notebookDataOutputThreadLocal.set(notebookDataOutput);
		blockStateMapThreadLocal.set(map);
	}

	@Inject(method = "run", at = @At("TAIL"))
	private void runTail(CachedOutput output, CallbackInfoReturnable<CompletableFuture<?>> cir) {
		notebookDataOutputThreadLocal.remove();
		blockStateMapThreadLocal.remove();
	}

	@Inject(method = "method_25738", at = @At("HEAD"), cancellable = true)
	private static void filterBlocksForProcessingMod(Map<Block, BlockStateGenerator> map, Block block, CallbackInfoReturnable<Boolean> cir) {
		NotebookDataOutput dataOutput = notebookDataOutputThreadLocal.get();

		if (dataOutput != null) {
			if (!dataOutput.isStrictValidationEnabled()) {
				cir.setReturnValue(false);
				return;
			}

			if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(dataOutput.getModId())) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "method_25741", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/models/model/ModelLocationUtils;getModelLocation(Lnet/minecraft/world/item/Item;)Lnet/minecraft/resources/ResourceLocation;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void filterItemsForProcessingMod(Set<Item> set, Map<ResourceLocation, Supplier<JsonElement>> map, Block block, CallbackInfo ci, Item item) {
		NotebookDataOutput dataOutput = notebookDataOutputThreadLocal.get();

		if (dataOutput != null) {
			// Only generate the item model if the block state json was registered
			if (!blockStateMapThreadLocal.get().containsKey(block)) {
				ci.cancel();
				return;
			}

			if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(dataOutput.getModId())) {
				ci.cancel();
			}
		}
	}
}

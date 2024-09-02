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

import net.minecraft.block.Block;
import net.minecraft.data.DataPackOutput;
import net.minecraft.data.DataWriter;
import net.minecraft.data.client.BlockStateDefinitionProvider;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.model.BlockStateModelGenerator;
import net.minecraft.item.Item;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.api.datagen.v1.provider.ModelDataProvider;

@Mixin(BlockStateDefinitionProvider.class)
public class ModelProviderMixin {
	@Unique
	private NotebookDataOutput notebookDataOutput;

	@Unique
	private static final ThreadLocal<NotebookDataOutput> notebookDataOutputThreadLocal = new ThreadLocal<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(DataPackOutput output, CallbackInfo info) {
		if (output instanceof NotebookDataOutput notebookDataOutput) {
			this.notebookDataOutput = notebookDataOutput;
		}
	}

	@Unique
	private static ThreadLocal<Map<Block, BlockStateModelGenerator>> blockStateMapThreadLocal = new ThreadLocal<>();

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/client/model/BlockStateModelGenerator;register()V"))
	private void registerBlockStateModels(BlockStateModelGenerator instance) {
		if (((Object) this) instanceof ModelDataProvider modelDataProvider) {
			modelDataProvider.generateBlockStateModels(instance);
		} else {
			instance.register();
		}
	}

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/client/ItemModelGenerator;register()V"))
	private void registerItemModels(ItemModelGenerator instance) {
		if (((Object) this) instanceof ModelDataProvider modelDataProvider) {
			modelDataProvider.generateItemModels(instance);
		} else {
			instance.register();
		}
	}

	@Inject(method = "run", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", ordinal = 0, remap = false))
	private void runHead(DataWriter cachedOutput, CallbackInfoReturnable<CompletableFuture<?>> cir, @Local Map<Block, BlockStateModelGenerator> map) {
		notebookDataOutputThreadLocal.set(notebookDataOutput);
		blockStateMapThreadLocal.set(map);
	}

	@Inject(method = "run", at = @At("TAIL"))
	private void runTail(DataWriter output, CallbackInfoReturnable<CompletableFuture<?>> cir) {
		notebookDataOutputThreadLocal.remove();
		blockStateMapThreadLocal.remove();
	}

	@Inject(method = "method_25738", at = @At("HEAD"), cancellable = true)
	private static void filterBlocksForProcessingMod(Map<Block, BlockStateModelGenerator> map, Block block, CallbackInfoReturnable<Boolean> cir) {
		NotebookDataOutput dataOutput = notebookDataOutputThreadLocal.get();

		if (dataOutput != null) {
			if (!dataOutput.isStrictValidationEnabled()) {
				cir.setReturnValue(false);
				return;
			}

			if (!BuiltInRegistries.BLOCK.getId(block).getNamespace().equals(dataOutput.getModId())) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "method_25741", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/client/model/ModelIds;getItemModelId(Lnet/minecraft/item/Item;)Lnet/minecraft/util/Identifier;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void filterItemsForProcessingMod(Set<Item> set, Map<Identifier, Supplier<JsonElement>> map, Block block, CallbackInfo ci, Item item) {
		NotebookDataOutput dataOutput = notebookDataOutputThreadLocal.get();

		if (dataOutput != null) {
			// Only generate the item model if the block state json was registered
			if (!blockStateMapThreadLocal.get().containsKey(block)) {
				ci.cancel();
				return;
			}

			if (!BuiltInRegistries.ITEM.getId(item).getNamespace().equals(dataOutput.getModId())) {
				ci.cancel();
			}
		}
	}
}

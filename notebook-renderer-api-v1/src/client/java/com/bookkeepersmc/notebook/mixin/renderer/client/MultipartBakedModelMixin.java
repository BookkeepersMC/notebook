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
package com.bookkeepersmc.notebook.mixin.renderer.client;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import com.bookkeepersmc.notebook.api.renderer.v1.model.NotebookBakedModel;
import com.bookkeepersmc.notebook.api.renderer.v1.render.RenderContext;

@Mixin(MultipartBakedModel.class)
public class MultipartBakedModelMixin implements NotebookBakedModel {
	@Shadow
	@Final
	private List<MultipartBakedModel.C_sirucsir> components;

	@Shadow
	@Final
	private Map<BlockState, BitSet> stateCache;

	@Unique
	boolean isVanilla = true;

	@Override
	public boolean isVanillaAdapter() {
		return isVanilla;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(List<MultipartBakedModel.C_sirucsir> components, CallbackInfo info) {
		for (MultipartBakedModel.C_sirucsir component : components) {
			if (!component.model().isVanillaAdapter()) {
				isVanilla = false;
				break;
			}
		}
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		BitSet bitSet = this.stateCache.get(state);

		if (bitSet == null) {
			bitSet = new BitSet();

			for (int i = 0; i < this.components.size(); i++) {
				MultipartBakedModel.C_sirucsir component = components.get(i);

				if (component.condition().test(state)) {
					bitSet.set(i);
				}
			}

			stateCache.put(state, bitSet);
		}

		RandomGenerator randomGenerator = randomSupplier.get();

		long randomSeed = randomGenerator.nextLong();
		Supplier<RandomGenerator> subModelRandomSupplier = () -> {
			randomGenerator.setSeed(randomSeed);
			return randomGenerator;
		};

		for (int i = 0; i < this.components.size(); i++) {
			if (bitSet.get(i)) {
				components.get(i).model().emitBlockQuads(blockView, state, pos, randomSupplier, context);
			}
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		// Vanilla MC does not use MultipartBakedModel for items.
	}
}

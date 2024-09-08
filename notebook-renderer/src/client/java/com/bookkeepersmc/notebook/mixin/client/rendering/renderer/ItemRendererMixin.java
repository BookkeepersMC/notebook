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
package com.bookkeepersmc.notebook.mixin.client.rendering.renderer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import com.bookkeepersmc.notebook.impl.client.rendering.renderer.render.ItemRenderContext;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	@Final
	@Shadow
	private ItemColors colors;

	@Unique
	private final ThreadLocal<ItemRenderContext> fabric_contexts = ThreadLocal.withInitial(() -> new ItemRenderContext(colors));

	@Inject(method = "method_62476", at = @At(value = "HEAD"), cancellable = true)
	public void hook_renderItem(ItemStack stack, ModelTransformationMode transformMode, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, BakedModel model, boolean notInHand, CallbackInfo ci) {
		if (!model.isVanillaAdapter()) {
			fabric_contexts.get().renderModel(stack, transformMode, matrixStack, vertexConsumerProvider, light, overlay, model);
			ci.cancel();
		}
	}
}

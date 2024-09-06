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
package com.bookkeepersmc.notebook.api.client.rendering.v1;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import com.bookkeepersmc.notebook.impl.client.rendering.BuiltinItemRendererRegistryImpl;

/**
 * This registry holds {@linkplain DynamicItemRenderer builtin item renderers} for items.
 */
public interface BuiltinItemRendererRegistry {
	/**
	 * The singleton instance of the renderer registry.
	 * Use this instance to call the methods in this interface.
	 */
	BuiltinItemRendererRegistry INSTANCE = new BuiltinItemRendererRegistryImpl();

	/**
	 * Registers the renderer for the item.
	 *
	 * <p>Note that the item's JSON model must also extend {@code minecraft:builtin/entity}.
	 *
	 * @param item     the item
	 * @param renderer the renderer
	 * @throws IllegalArgumentException if the item already has a registered renderer
	 * @throws NullPointerException if either the item or the renderer is null
	 */
	void register(ItemConvertible item, DynamicItemRenderer renderer);

	/**
	 * Returns the renderer for the item, or {@code null} if the item has no renderer.
	 */
	@Nullable
	DynamicItemRenderer get(ItemConvertible item);

	/**
	 * Dynamic item renderers render items with custom code.
	 * They allow using non-model rendering, such as BERs, for items.
	 *
	 * <p>An item with a dynamic renderer must have a model extending {@code minecraft:builtin/entity}.
	 * The renderers are registered with {@link BuiltinItemRendererRegistry#register(ItemConvertible, DynamicItemRenderer)}.
	 */
	@FunctionalInterface
	interface DynamicItemRenderer {
		/**
		 * Renders an item stack.
		 *
		 * @param stack           the rendered item stack
		 * @param mode            the model transformation mode
		 * @param matrices        the matrix stack
		 * @param vertexConsumers the vertex consumer provider
		 * @param light           packed lightmap coordinates
		 * @param overlay         the overlay UV passed to {@link com.mojang.blaze3d.vertex.VertexConsumer#uv1(int)}
		 */
		void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);
	}
}

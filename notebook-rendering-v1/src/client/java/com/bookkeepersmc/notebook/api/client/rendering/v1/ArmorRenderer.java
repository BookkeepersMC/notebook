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

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.impl.client.rendering.ArmorRendererRegistryImpl;

/**
 * Armor renderers render worn armor items with custom code.
 * They may be used to render armor with special models or effects.
 *
 * <p>The renderers are registered with {@link com.bookkeepersmc.notebook.api.client.rendering.v1.ArmorRenderer#register(ArmorRenderer, ItemConvertible...)}.
 */
@FunctionalInterface
public interface ArmorRenderer {
	/**
	 * Registers the armor renderer for the specified items.
	 * @param renderer	the renderer
	 * @param items		the items
	 * @throws IllegalArgumentException if an item already has a registered armor renderer
	 * @throws NullPointerException if either an item or the renderer is null
	 */
	static void register(ArmorRenderer renderer, ItemConvertible... items) {
		ArmorRendererRegistryImpl.register(renderer, items);
	}
	/**
	 * Helper method for rendering a specific armor model, comes after setting visibility.
	 *
	 * <p>This primarily handles applying glint and the correct {@link RenderLayer}
	 * @param matrices			the matrix stack
	 * @param vertexConsumers	the vertex consumer provider
	 * @param light				packed lightmap coordinates
	 * @param stack				the item stack of the armor item
	 * @param model				the model to be rendered
	 * @param texture			the texture to be applied
	 */
	static void register(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, Model model, Identifier texture) {
		VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(texture), stack.hasGlint());
		model.method_62100(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
	}

	/**
	 * Renders an armor part.
	 *
	 * @param matrices			     the matrix stack
	 * @param vertexConsumers	     the vertex consumer provider
	 * @param stack				     the item stack of the armor item
	 * @param bipedEntityRenderState the render state of the entity
	 * @param slot				     the equipment slot in which the armor stack is worn
	 * @param light				     packed lightmap coordinates
	 * @param contextModel		     the model provided by {@link FeatureRenderer#getContextModel()}
	 */
	void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, BipedEntityRenderState bipedEntityRenderState, EquipmentSlot slot, int light, BipedEntityModel<BipedEntityRenderState> contextModel);
}

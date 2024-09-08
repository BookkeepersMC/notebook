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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer.render;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.random.RandomGenerator;

import com.bookkeepersmc.notebook.api.renderer.v1.material.BlendMode;
import com.bookkeepersmc.notebook.api.renderer.v1.material.RenderMaterial;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadEmitter;
import com.bookkeepersmc.notebook.api.util.TriState;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.helper.ColorHelper;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.EncodingFormat;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.MutableQuadViewImpl;
import com.bookkeepersmc.notebook.impl.renderer.VanillaModelEncoder;
import com.bookkeepersmc.notebook.mixin.client.rendering.renderer.ItemRendererAccessor;

/**
 * The render context used for item rendering.
 */
public class ItemRenderContext extends AbstractRenderContext {
	/** Value vanilla uses for item rendering.  The only sensible choice, of course.  */
	private static final long ITEM_RANDOM_SEED = 42L;

	private final ItemColors colorMap;
	private final RandomGenerator random = RandomGenerator.createLegacy();
	private final Supplier<RandomGenerator> randomSupplier = () -> {
		random.setSeed(ITEM_RANDOM_SEED);
		return random;
	};

	private final MutableQuadViewImpl editorQuad = new MutableQuadViewImpl() {
		{
			data = new int[EncodingFormat.TOTAL_STRIDE];
			clear();
		}

		@Override
		public void emitDirectly() {
			renderQuad(this);
		}
	};

	private final BakedModelConsumerImpl vanillaModelConsumer = new BakedModelConsumerImpl();

	private ItemStack itemStack;
	private ModelTransformationMode transformMode;
	private MatrixStack matrixStack;
	private VertexConsumerProvider vertexConsumerProvider;
	private int lightmap;

	private boolean isDefaultTranslucent;
	private boolean isDefaultGlint;
	private boolean isGlintDynamicDisplay;

	private MatrixStack.Entry dynamicDisplayGlintEntry;
	private VertexConsumer translucentVertexConsumer;
	private VertexConsumer cutoutVertexConsumer;
	private VertexConsumer translucentGlintVertexConsumer;
	private VertexConsumer cutoutGlintVertexConsumer;

	public ItemRenderContext(ItemColors colorMap) {
		this.colorMap = colorMap;
	}

	@Override
	public QuadEmitter getEmitter() {
		editorQuad.clear();
		return editorQuad;
	}

	@Override
	public boolean isFaceCulled(@Nullable Direction face) {
		throw new IllegalStateException("isFaceCulled can only be called on a block render context.");
	}

	@Override
	public ModelTransformationMode itemTransformationMode() {
		return transformMode;
	}

	@Override
	public BakedModelConsumer bakedModelConsumer() {
		return vanillaModelConsumer;
	}

	public void renderModel(ItemStack itemStack, ModelTransformationMode transformMode, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int lightmap, int overlay, BakedModel model) {
		this.itemStack = itemStack;
		this.transformMode = transformMode;
		this.matrixStack = matrixStack;
		this.vertexConsumerProvider = vertexConsumerProvider;
		this.lightmap = lightmap;
		this.overlay = overlay;
		computeOutputInfo();

		matrix = matrixStack.peek().getModel();
		normalMatrix = matrixStack.peek().getNormal();

		model.emitItemQuads(itemStack, randomSupplier, this);

		this.itemStack = null;
		this.matrixStack = null;
		this.vertexConsumerProvider = null;

		dynamicDisplayGlintEntry = null;
		translucentVertexConsumer = null;
		cutoutVertexConsumer = null;
		translucentGlintVertexConsumer = null;
		cutoutGlintVertexConsumer = null;
	}

	private void computeOutputInfo() {
		isDefaultTranslucent = RenderLayers.method_23678(itemStack) == TexturedRenderLayers.getItemEntityTranslucentCull();
		isDefaultGlint = itemStack.hasGlint();
		isGlintDynamicDisplay = ItemRendererAccessor.fabric_callUsesDynamicDisplay(itemStack);
	}

	private void renderQuad(MutableQuadViewImpl quad) {
		if (!transform(quad)) {
			return;
		}

		final RenderMaterial mat = quad.material();
		final int colorIndex = mat.disableColorIndex() ? -1 : quad.colorIndex();
		final boolean emissive = mat.emissive();
		final VertexConsumer vertexConsumer = getVertexConsumer(mat.blendMode(), mat.glint());

		colorizeQuad(quad, colorIndex);
		shadeQuad(quad, emissive);
		bufferQuad(quad, vertexConsumer);
	}

	private void colorizeQuad(MutableQuadViewImpl quad, int colorIndex) {
		if (colorIndex != -1) {
			final int itemColor = colorMap.getColor(itemStack, colorIndex);

			for (int i = 0; i < 4; i++) {
				quad.color(i, ColorHelper.multiplyColor(itemColor, quad.color(i)));
			}
		}
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
		if (emissive) {
			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, LightmapTextureManager.MAX_LIGHT_COORDINATE);
			}
		} else {
			final int lightmap = this.lightmap;

			for (int i = 0; i < 4; i++) {
				quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), lightmap));
			}
		}
	}

	/**
	 * Caches custom blend mode / vertex consumers and mimics the logic
	 * in {@code RenderLayers.getItemLayer}. Layers other than
	 * translucent are mapped to cutout.
	 */
	private VertexConsumer getVertexConsumer(BlendMode blendMode, TriState glintMode) {
		boolean translucent;
		boolean glint;

		if (blendMode == BlendMode.DEFAULT) {
			translucent = isDefaultTranslucent;
		} else {
			translucent = blendMode == BlendMode.TRANSLUCENT;
		}

		if (glintMode == TriState.DEFAULT) {
			glint = isDefaultGlint;
		} else {
			glint = glintMode == TriState.TRUE;
		}

		if (translucent) {
			if (glint) {
				if (translucentGlintVertexConsumer == null) {
					translucentGlintVertexConsumer = createVertexConsumer(TexturedRenderLayers.getItemEntityTranslucentCull(), true);
				}

				return translucentGlintVertexConsumer;
			} else {
				if (translucentVertexConsumer == null) {
					translucentVertexConsumer = createVertexConsumer(TexturedRenderLayers.getItemEntityTranslucentCull(), false);
				}

				return translucentVertexConsumer;
			}
		} else {
			if (glint) {
				if (cutoutGlintVertexConsumer == null) {
					cutoutGlintVertexConsumer = createVertexConsumer(TexturedRenderLayers.getEntityCutout(), true);
				}

				return cutoutGlintVertexConsumer;
			} else {
				if (cutoutVertexConsumer == null) {
					cutoutVertexConsumer = createVertexConsumer(TexturedRenderLayers.getEntityCutout(), false);
				}

				return cutoutVertexConsumer;
			}
		}
	}

	private VertexConsumer createVertexConsumer(RenderLayer layer, boolean glint) {
		if (isGlintDynamicDisplay && glint) {
			if (dynamicDisplayGlintEntry == null) {
				dynamicDisplayGlintEntry = matrixStack.peek().copy();

				if (transformMode == ModelTransformationMode.GUI) {
					MatrixUtil.multiplyComponentWise(dynamicDisplayGlintEntry.getModel(), 0.5F);
				} else if (transformMode.isFirstPerson()) {
					MatrixUtil.multiplyComponentWise(dynamicDisplayGlintEntry.getModel(), 0.75F);
				}
			}

			return ItemRenderer.getCompassGlintConsumer(vertexConsumerProvider, layer, dynamicDisplayGlintEntry);
		}

		return ItemRenderer.getItemGlintConsumer(vertexConsumerProvider, layer, true, glint);
	}

	private class BakedModelConsumerImpl implements BakedModelConsumer {
		@Override
		public void accept(BakedModel model) {
			accept(model, null);
		}

		@Override
		public void accept(BakedModel model, @Nullable BlockState state) {
			VanillaModelEncoder.emitItemQuads(model, state, randomSupplier, ItemRenderContext.this);
		}
	}
}

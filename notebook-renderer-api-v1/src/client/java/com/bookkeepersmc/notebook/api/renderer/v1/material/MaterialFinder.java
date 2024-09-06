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
package com.bookkeepersmc.notebook.api.renderer.v1.material;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;

import com.bookkeepersmc.notebook.api.util.TriState;

public interface MaterialFinder extends MaterialView {
	/**
	 * Controls how sprite pixels should be blended with the scene.
	 *
	 * <p>The default value is {@link BlendMode#DEFAULT}.
	 *
	 * @see BlendMode
	 */
	MaterialFinder blendMode(BlendMode blendMode);

	/**
	 * Controls whether vertex colors should be modified for quad coloring. This property
	 * is inverted, so a value of {@code false} means that quad coloring will be applied.
	 *
	 * <p>The default value is {@code false}.
	 */
	MaterialFinder disableColorIndex(boolean disable);

	/**
	 * When true, sprite texture and color will be rendered at full brightness.
	 * Lightmap values provided via {@link com.bookkeepersmc.notebook.api.renderer.v1.mesh.QuadEmitter#lightmap(int)} will be ignored.
	 *
	 * <p>This is the preferred method for emissive lighting effects. Some renderers
	 * with advanced lighting pipelines may not use block lightmaps and this method will
	 * allow per-sprite emissive lighting in future extensions that support overlay sprites.
	 *
	 * <p>Note that color will still be modified by diffuse shading and ambient occlusion,
	 * unless disabled via {@link #disableDiffuse(boolean)} and {@link #ambientOcclusion(TriState)}.
	 *
	 * <p>The default value is {@code false}.
	 */
	MaterialFinder emissive(boolean isEmissive);

	/**
	 * Controls whether vertex colors should be modified for diffuse shading. This property
	 * is inverted, so a value of {@code false} means that diffuse shading will be applied.
	 *
	 * <p>The default value is {@code false}.
	 *
	 * <p>This property is guaranteed to be respected in block contexts. Some renderers may also respect it in item
	 * contexts, but this is not guaranteed.
	 */
	MaterialFinder disableDiffuse(boolean disable);

	/**
	 * Controls whether vertex colors should be modified for ambient occlusion.
	 *
	 * <p>If set to {@link TriState#DEFAULT}, ambient occlusion will be used if
	 * {@linkplain BakedModel#useAmbientOcclusion() the model uses ambient occlusion} and the block state has
	 * {@linkplain BlockState#getLuminance() a luminance} of 0. Set to {@link TriState#TRUE} or {@link TriState#FALSE}
	 * to override this behavior. {@link TriState#TRUE} will not have an effect if
	 * {@linkplain net.minecraft.client.Minecraft#isAmbientOcclusionEnabled() ambient occlusion is disabled globally}.
	 *
	 * <p>The default value is {@link TriState#DEFAULT}.
	 *
	 * <p>This property is respected only in block contexts. It will not have an effect in other contexts.
	 */
	MaterialFinder ambientOcclusion(TriState mode);

	/**
	 * Controls whether glint should be applied.
	 *
	 * <p>If set to {@link TriState#DEFAULT}, glint will be applied in item contexts if
	 * {@linkplain ItemStack#hasGlint() the item stack has glint}. Set to {@link TriState#TRUE} or
	 * {@link TriState#FALSE} to override this behavior.
	 *
	 * <p>The default value is {@link TriState#DEFAULT}.
	 *
	 * <p>This property is guaranteed to be respected in item contexts. Some renderers may also respect it in block
	 * contexts, but this is not guaranteed.
	 */
	MaterialFinder glint(TriState mode);

	/**
	 * A hint to the renderer about how the quad is intended to be shaded, for example through ambient occlusion and
	 * diffuse shading. The renderer is free to ignore this hint.
	 *
	 * <p>The default value is {@link ShadeMode#ENHANCED}.
	 *
	 * <p>This property is respected only in block contexts. It will not have an effect in other contexts.
	 *
	 * @see ShadeMode
	 *
	 * @apiNote The default implementation will be removed in the next breaking release.
	 */
	default MaterialFinder shadeMode(ShadeMode mode) {
		return this;
	}

	/**
	 * Copies all properties from the given {@link MaterialView} to this material finder.
	 */
	MaterialFinder copyFrom(MaterialView material);

	/**
	 * Resets this instance to default values. Values will match those
	 * in effect when an instance is newly obtained via {@link com.bookkeepersmc.notebook.api.renderer.v1.Renderer#materialFinder()}.
	 */
	MaterialFinder clear();

	/**
	 * Returns the standard material encoding all
	 * of the current settings in this finder. The settings in
	 * this finder are not changed.
	 *
	 * <p>Resulting instances can and should be re-used to prevent
	 * needless memory allocation. {@link com.bookkeepersmc.notebook.api.renderer.v1.Renderer} implementations
	 * may or may not cache standard material instances.
	 */
	RenderMaterial find();
}

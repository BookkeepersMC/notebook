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
package com.bookkeepersmc.notebook.mixin.resource.loader.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;

import com.bookkeepersmc.notebook.api.resource.IdentifiableResourceReloadListener;
import com.bookkeepersmc.notebook.api.resource.ResourceReloadListenerKeys;

@Mixin({
		/* public */
		SoundManager.class, ModelManager.class, LanguageManager.class, TextureManager.class,
		/* private */
		LevelRenderer.class, BlockRenderDispatcher.class, ItemRenderer.class
})
public abstract class KeyedResourceReloadListenerClientMixin implements IdentifiableResourceReloadListener {
	private ResourceLocation notebook$id;
	private Collection<ResourceLocation> notebook$depdencies;

	@Override
	public ResourceLocation getNotebookId() {
		if (this.notebook$id == null) {
			Object self = this;

			if (self instanceof SoundManager) {
				this.notebook$id = ResourceReloadListenerKeys.SOUNDS;
			} else if (self instanceof ModelManager) {
				this.notebook$id = ResourceReloadListenerKeys.MODELS;
			} else if (self instanceof LanguageManager) {
				this.notebook$id = ResourceReloadListenerKeys.LANGUAGES;
			} else if (self instanceof TextureManager) {
				this.notebook$id = ResourceReloadListenerKeys.TEXTURES;
			} else {
				this.notebook$id = ResourceLocation.withDefaultNamespace("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.notebook$id;
	}

	@Override
	@SuppressWarnings({"ConstantConditions"})
	public Collection<ResourceLocation> getNotebookDependencies() {
		if (this.notebook$depdencies == null) {
			Object self = this;

			if (self instanceof ModelManager || self instanceof LevelRenderer) {
				this.notebook$depdencies = Collections.singletonList(ResourceReloadListenerKeys.TEXTURES);
			} else if (self instanceof ItemRenderer || self instanceof BlockRenderDispatcher) {
				this.notebook$depdencies = Collections.singletonList(ResourceReloadListenerKeys.MODELS);
			} else {
				this.notebook$depdencies = Collections.emptyList();
			}
		}

		return this.notebook$depdencies;
	}
}

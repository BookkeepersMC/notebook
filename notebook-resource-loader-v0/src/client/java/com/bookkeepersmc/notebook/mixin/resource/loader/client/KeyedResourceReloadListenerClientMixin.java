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

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.resource.IdentifiableResourceReloadListener;
import com.bookkeepersmc.notebook.api.resource.ResourceReloadListenerKeys;

@Mixin({
		/* public */
		SoundManager.class, BakedModelManager.class, LanguageManager.class, TextureManager.class,
		/* private */
		WorldRenderer.class, BlockRenderManager.class, ItemRenderer.class
})
public abstract class KeyedResourceReloadListenerClientMixin implements IdentifiableResourceReloadListener {
	private Identifier notebook$id;
	private Collection<Identifier> notebook$depdencies;

	@Override
	public Identifier getNotebookId() {
		if (this.notebook$id == null) {
			Object self = this;

			if (self instanceof SoundManager) {
				this.notebook$id = ResourceReloadListenerKeys.SOUNDS;
			} else if (self instanceof BakedModelManager) {
				this.notebook$id = ResourceReloadListenerKeys.MODELS;
			} else if (self instanceof LanguageManager) {
				this.notebook$id = ResourceReloadListenerKeys.LANGUAGES;
			} else if (self instanceof TextureManager) {
				this.notebook$id = ResourceReloadListenerKeys.TEXTURES;
			} else {
				this.notebook$id = Identifier.ofDefault("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.notebook$id;
	}

	@Override
	@SuppressWarnings({"ConstantConditions"})
	public Collection<Identifier> getNotebookDependencies() {
		if (this.notebook$depdencies == null) {
			Object self = this;

			if (self instanceof BakedModelManager || self instanceof WorldRenderer) {
				this.notebook$depdencies = Collections.singletonList(ResourceReloadListenerKeys.TEXTURES);
			} else if (self instanceof ItemRenderer || self instanceof BlockRenderManager) {
				this.notebook$depdencies = Collections.singletonList(ResourceReloadListenerKeys.MODELS);
			} else {
				this.notebook$depdencies = Collections.emptyList();
			}
		}

		return this.notebook$depdencies;
	}
}

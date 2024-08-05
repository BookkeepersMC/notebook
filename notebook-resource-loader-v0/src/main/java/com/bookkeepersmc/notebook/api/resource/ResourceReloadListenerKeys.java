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
package com.bookkeepersmc.notebook.api.resource;

import net.minecraft.resources.ResourceLocation;

/**
 * This class contains default keys for various Minecraft resource reload listeners.
 *
 * @see IdentifiableResourceReloadListener
 */
public final class ResourceReloadListenerKeys {
	// client
	public static final ResourceLocation SOUNDS = ResourceLocation.withDefaultNamespace("sounds");
	public static final ResourceLocation FONTS = ResourceLocation.withDefaultNamespace("fonts");
	public static final ResourceLocation MODELS = ResourceLocation.withDefaultNamespace("models");
	public static final ResourceLocation LANGUAGES = ResourceLocation.withDefaultNamespace("languages");
	public static final ResourceLocation TEXTURES = ResourceLocation.withDefaultNamespace("textures");

	// server
	public static final ResourceLocation TAGS = ResourceLocation.withDefaultNamespace("tags");
	public static final ResourceLocation RECIPES = ResourceLocation.withDefaultNamespace("recipes");
	public static final ResourceLocation ADVANCEMENTS = ResourceLocation.withDefaultNamespace("advancements");
	public static final ResourceLocation FUNCTIONS = ResourceLocation.withDefaultNamespace("functions");

	private ResourceReloadListenerKeys() { }
}

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

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import com.bookkeepersmc.notebook.impl.client.rendering.EntityRendererRegistryImpl;

/**
 * Helper class for registering EntityRenderers.
 */
public final class EntityRendererRegistry {
	/**
	 * Register a BlockEntityRenderer for a BlockEntityType. Can be called clientside before the world is rendered.
	 *
	 * @param entityType the {@link EntityType} to register a renderer for
	 * @param entityRendererFactory a {@link EntityRendererFactory} that creates a {@link net.minecraft.client.render.entity.EntityRenderer}, called
	 *                            when {@link net.minecraft.client.render.entity.EntityRenderDispatcher} is initialized or immediately if the dispatcher
	 *                            class is already loaded
	 * @param <E> the {@link Entity}
	 */
	public static <E extends Entity> void register(EntityType<? extends E> entityType, EntityRendererFactory<E> entityRendererFactory) {
		EntityRendererRegistryImpl.register(entityType, entityRendererFactory);
	}

	private EntityRendererRegistry() {
	}
}

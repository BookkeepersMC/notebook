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
package com.bookkeepersmc.notebook.impl.client.rendering;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.DimensionVisualEffects;
import net.minecraft.registry.ResourceKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import com.bookkeepersmc.notebook.api.client.rendering.v1.DimensionRenderingRegistry;
import com.bookkeepersmc.notebook.api.client.rendering.v1.DimensionRenderingRegistry.CloudRenderer;
import com.bookkeepersmc.notebook.api.client.rendering.v1.DimensionRenderingRegistry.SkyRenderer;
import com.bookkeepersmc.notebook.api.client.rendering.v1.DimensionRenderingRegistry.WeatherRenderer;
import com.bookkeepersmc.notebook.mixin.client.rendering.DimensionVisualEffectsAccessor;

public final class DimensionRenderingRegistryImpl {
	private static final Map<ResourceKey<World>, SkyRenderer> SKY_RENDERERS = new IdentityHashMap<>();
	private static final Map<ResourceKey<World>, CloudRenderer> CLOUD_RENDERERS = new IdentityHashMap<>();
	private static final Map<ResourceKey<World>, WeatherRenderer> WEATHER_RENDERERS = new IdentityHashMap<>();

	public static void registerSkyRenderer(ResourceKey<World> key, DimensionRenderingRegistry.SkyRenderer renderer) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(renderer);

		SKY_RENDERERS.putIfAbsent(key, renderer);
	}

	public static void registerWeatherRenderer(ResourceKey<World> key, WeatherRenderer renderer) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(renderer);

		WEATHER_RENDERERS.putIfAbsent(key, renderer);
	}

	public static void registerDimensionEffects(Identifier key, DimensionVisualEffects effects) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(effects);
		//The map containing all dimension effects returns a default if null so a null check doesn't work.

		DimensionVisualEffectsAccessor.getIdentifierMap().putIfAbsent(key, effects);
	}

	public static void registerCloudRenderer(ResourceKey<World> key, CloudRenderer renderer) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(renderer);

		CLOUD_RENDERERS.putIfAbsent(key, renderer);
	}

	@Nullable
	public static SkyRenderer getSkyRenderer(ResourceKey<World> key) {
		return SKY_RENDERERS.get(key);
	}

	@Nullable
	public static CloudRenderer getCloudRenderer(ResourceKey<World> key) {
		return CLOUD_RENDERERS.get(key);
	}

	@Nullable
	public static WeatherRenderer getWeatherRenderer(ResourceKey<World> key) {
		return WEATHER_RENDERERS.get(key);
	}

	@Nullable
	public static DimensionVisualEffects getDimensionEffects(Identifier key) {
		return DimensionVisualEffectsAccessor.getIdentifierMap().get(key);
	}
}

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

import net.minecraft.client.render.DimensionVisualEffects;
import net.minecraft.registry.ResourceKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import com.bookkeepersmc.notebook.impl.client.rendering.DimensionRenderingRegistryImpl;

public interface DimensionRenderingRegistry {
	/**
	 * Registers the custom sky renderer for a {@link World}.
	 *
	 * <p>This overrides Vanilla's sky rendering.
	 * @param key A {@link ResourceKey} for your {@link World}
	 * @param renderer A {@link SkyRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerSkyRenderer(ResourceKey<World> key, SkyRenderer renderer) {
		DimensionRenderingRegistryImpl.registerSkyRenderer(key, renderer);
	}

	/**
	 * Registers a custom weather renderer for a {@link World}.
	 *
	 * <p>This overrides Vanilla's weather rendering.
	 * @param key A RegistryKey for your {@link World}
	 * @param renderer A {@link WeatherRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerWeatherRenderer(ResourceKey<World> key, WeatherRenderer renderer) {
		DimensionRenderingRegistryImpl.registerWeatherRenderer(key, renderer);
	}

	/**
	 * Registers dimension effects for an {@link net.minecraft.util.Identifier}.
	 *
	 * <p>This registers a new option for the "effects" entry of the dimension type json.
	 *
	 * @param key     The {@link net.minecraft.util.Identifier} for the new option entry.
	 * @param effects The {@link DimensionVisualEffects} option.
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerDimensionEffects(Identifier key, DimensionVisualEffects effects) {
		DimensionRenderingRegistryImpl.registerDimensionEffects(key, effects);
	}

	/**
	 * Registers a custom cloud renderer for a {@link World}.
	 *
	 * <p>This overrides Vanilla's cloud rendering.
	 *
	 * @param key      A {@link ResourceKey} for your {@link World}
	 * @param renderer A {@link CloudRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerCloudRenderer(ResourceKey<World> key, CloudRenderer renderer) {
		DimensionRenderingRegistryImpl.registerCloudRenderer(key, renderer);
	}

	/**
	 * Gets the custom sky renderer for the given {@link World}.
	 *
	 * @param key A {@link ResourceKey} for your {@link World}
	 * @return {@code null} if no custom sky renderer is registered for the dimension.
	 */
	@Nullable
	static SkyRenderer getSkyRenderer(ResourceKey<World> key) {
		return DimensionRenderingRegistryImpl.getSkyRenderer(key);
	}

	/**
	 * Gets the custom cloud renderer for the given {@link World}.
	 *
	 * @param key A {@link ResourceKey} for your {@link World}
	 * @return {@code null} if no custom cloud renderer is registered for the dimension.
	 */
	@Nullable
	static CloudRenderer getCloudRenderer(ResourceKey<World> key) {
		return DimensionRenderingRegistryImpl.getCloudRenderer(key);
	}

	/**
	 * Gets the custom weather effect renderer for the given {@link World}.
	 *
	 * @return {@code null} if no custom weather effect renderer is registered for the dimension.
	 */
	@Nullable
	static WeatherRenderer getWeatherRenderer(ResourceKey<World> key) {
		return DimensionRenderingRegistryImpl.getWeatherRenderer(key);
	}

	/**
	 * Gets the dimension effects registered for an id.
	 * @param key A {@link ResourceKey} for your {@link World}.
	 * @return overworld effect if no dimension effects is registered for the key.
	 */
	@Nullable
	static DimensionVisualEffects getDimensionEffects(Identifier key) {
		return DimensionRenderingRegistryImpl.getDimensionEffects(key);
	}

	@FunctionalInterface
	interface SkyRenderer {
		void render(WorldRenderContext context);
	}

	@FunctionalInterface
	interface WeatherRenderer {
		void render(WorldRenderContext context);
	}

	@FunctionalInterface
	interface CloudRenderer {
		void render(WorldRenderContext context);
	}
}

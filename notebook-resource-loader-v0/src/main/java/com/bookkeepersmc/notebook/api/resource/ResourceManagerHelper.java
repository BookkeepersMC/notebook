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

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.HolderLookup;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.notebook.impl.resource.loader.ResourceManagerHelperImpl;

/**
 * Helper for working with {@link ResourceManager} instances, and other resource loader generalities.
 */
@ApiStatus.NonExtendable
public interface ResourceManagerHelper {
	/**
	 * Add a resource reload listener for a given registry.
	 *
	 * @param listener The resource reload listener.
	 * @deprecated Use {@link ResourceManagerHelper#registerReloadListener(IdentifiableResourceReloadListener)}
	 */
	@Deprecated
	default void addReloadListener(IdentifiableResourceReloadListener listener) {
		registerReloadListener(listener);
	}

	/**
	 * Register a resource reload listener for a given resource manager type.
	 *
	 * @param listener The resource reload listener.
	 */
	void registerReloadListener(IdentifiableResourceReloadListener listener);

	/**
	 * Register a resource reload listener for a given resource manager type.
	 *
	 * <p>Note: This is only supported for server data reload listeners.
	 *
	 * @param identifier The identifier of the listener.
	 * @param listenerFactory   A function that creates a new instance of the listener with a given registry lookup.
	 */
	void registerReloadListener(Identifier identifier, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory);

	/**
	 * Get the ResourceManagerHelper instance for a given resource type.
	 *
	 * @param type The given resource type.
	 * @return The ResourceManagerHelper instance.
	 */
	static ResourceManagerHelper get(ResourceType type) {
		return ResourceManagerHelperImpl.get(type);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, ResourcePackActivationType activationType) {
		return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.getPath(), container, activationType);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param displayName    the display name of the resource pack, should include mod name for clarity
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, Text displayName, ResourcePackActivationType activationType) {
		return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.getPath(), container, displayName, activationType);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param displayName    the display name of the resource pack, should include mod name for clarity
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @deprecated Use {@link #registerBuiltinResourcePack(Identifier, ModContainer, Text, ResourcePackActivationType)} instead.
	 */
	@Deprecated
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, String displayName, ResourcePackActivationType activationType) {
		return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.getPath(), container, Text.literal(displayName), activationType);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The {@code subPath} corresponds to a path in the JAR file which points to the resource pack folder. For example the subPath can be {@code "resourcepacks/extra"}.
	 *
	 * <p>Note about the enabled by default parameter: a resource pack cannot be enabled by default, only data packs can.
	 * Making this work for resource packs is near impossible without touching how Vanilla handles disabled resource packs.
	 *
	 * @param id               the identifier of the resource pack
	 * @param subPath          the sub path in the mod resources
	 * @param container        the mod container
	 * @param enabledByDefault {@code true} if enabled by default, else {@code false}
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @deprecated Please use {@link #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)} instead, the {@code sub path} should be removed in a future
	 * release in favor of the identifier path.
	 */
	@Deprecated
	static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, boolean enabledByDefault) {
		return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, subPath, container,
				enabledByDefault ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL);
	}
}

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
package com.bookkeepersmc.notebook.api.event.registry;

import java.util.EnumSet;

import com.mojang.serialization.Lifecycle;

import net.minecraft.registry.DefaultMappedRegistry;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistrationInfo;
import net.minecraft.registry.Registry;
import net.minecraft.registry.ResourceKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.mixin.registry.sync.RegistriesAccessor;

public final class NotebookRegistryBuilder<T, R extends MutableRegistry<T>> {

	public static <T, R extends MutableRegistry<T>> NotebookRegistryBuilder<T, R> from(R registry) {
		return new NotebookRegistryBuilder<>(registry);
	}

	public static <T> NotebookRegistryBuilder<T, SimpleRegistry<T>> createSimple(ResourceKey<Registry<T>> registryKey) {
		return from(new SimpleRegistry<>(registryKey, Lifecycle.stable(), false));
	}

	public static <T> NotebookRegistryBuilder<T, DefaultMappedRegistry<T>> createDefaulted(ResourceKey<Registry<T>> registryKey, Identifier defaultId) {
		return from(new DefaultMappedRegistry<T>(defaultId.toString(), registryKey, Lifecycle.stable(), false));
	}

	private final R registry;
	private final EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);

	private NotebookRegistryBuilder(R registry) {
		this.registry = registry;
		attribute(RegistryAttribute.MODDED);
	}

	public NotebookRegistryBuilder<T, R> attribute(RegistryAttribute attribute) {
		attributes.add(attribute);
		return this;
	}

	public R buildAndRegister() {
		final ResourceKey<?> key = registry.getKey();

		for (RegistryAttribute attribute : attributes) {
			RegistryAttributeHolder.get(key).addAttribute(attribute);
		}

		RegistriesAccessor.getINTERNAL_ROOT().register((ResourceKey<MutableRegistry<?>>) key, registry, RegistrationInfo.DEFAULT);

		return registry;
	}
}

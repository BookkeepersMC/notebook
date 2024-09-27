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
package com.bookkeepersmc.notebook.api.item.v1;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.component.DataComponentType;

/**
 * Extensions for {@link net.minecraft.component.DataComponentMap.Builder}.
 *
 * <p>Note: This interface is automatically implemented on all component map builders via Mixin and interface injection.
 */
@ApiStatus.NonExtendable
public interface NotebookDataComponentMapBuilder {
	/**
	 * Gets the current value for the component type in the builder, or creates and adds a new value if it is not present.
	 *
	 * @param type     The component type
	 * @param fallback The supplier for the default data value if the type is not in this map yet. The value given by this supplier
	 *                 may not be null.
	 * @param <T>      The type of the component data
	 * @return Returns the current value in the map builder, or the default value provided by the fallback if not present
	 * @see #getOrEmpty(DataComponentType)
	 */
	default <T> T getOrCreate(DataComponentType<T> type, Supplier<@NotNull T> fallback) {
		throw new AssertionError("Implemented in Mixin");
	}

	/**
	 * Gets the current value for the component type in the builder, or creates and adds a new value if it is not present.
	 *
	 * @param type         The component type
	 * @param defaultValue The default data value if the type is not in this map yet
	 * @param <T>          The type of the component data
	 * @return Returns the current value in the map builder, or the default value if not present
	 */
	default <T> T getOrDefault(DataComponentType<T> type, @NotNull T defaultValue) {
		Objects.requireNonNull(defaultValue, "Cannot insert null values to component map builder");
		return getOrCreate(type, () -> defaultValue);
	}

	/**
	 * For list component types specifically, returns a mutable list of values currently held in the builder for the given
	 * component type. If the type is not registered to this builder yet, this will create and add a new empty list to the builder
	 * for the type, and return that.
	 *
	 * @param type The component type. The component must be a list-type.
	 * @param <T>  The type of the component entry data
	 * @return Returns a mutable list of values for the type.
	 */
	default <T> List<T> getOrEmpty(DataComponentType<List<T>> type) {
		throw new AssertionError("Implemented in Mixin");
	}
}

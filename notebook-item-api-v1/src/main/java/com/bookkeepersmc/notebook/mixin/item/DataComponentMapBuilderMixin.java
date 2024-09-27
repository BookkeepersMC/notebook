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
package com.bookkeepersmc.notebook.mixin.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.component.DataComponentMap;
import net.minecraft.component.DataComponentType;

import com.bookkeepersmc.notebook.api.item.v1.NotebookDataComponentMapBuilder;

@Mixin(DataComponentMap.Builder.class)
abstract class DataComponentMapBuilderMixin implements NotebookDataComponentMapBuilder {
	@Shadow
	@Final
	private Reference2ObjectMap<DataComponentType<?>, Object> components;

	@Shadow
	public abstract <T> DataComponentMap.Builder put(DataComponentType<T> type, @Nullable T value);

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getOrCreate(DataComponentType<T> type, Supplier<@NotNull T> fallback) {
		if (!this.components.containsKey(type)) {
			T defaultValue = fallback.get();
			Objects.requireNonNull(defaultValue, "Cannot insert null values to component map builder");
			this.put(type, defaultValue);
		}
		return (T) this.components.get(type);
	}
	@Override
	public <T> List<T> getOrEmpty(DataComponentType<List<T>> type) {
		// creating a new array list guarantees that the list in the map is mutable
		List<T> existing = new ArrayList<>(this.getOrCreate(type, Collections::emptyList));
		this.put(type, existing);
		return existing;
	}
}

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
package com.bookkeepersmc.notebook.impl.datagen;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;

public class ForcedTagEntry extends TagEntry {
	private final TagEntry entry;

	public ForcedTagEntry(TagEntry entry) {
		super(entry.id, true, entry.required);
		this.entry = entry;
	}

	@Override
	public <T> boolean build(Lookup<T> lookup, Consumer<T> consumer) {
		return entry.build(lookup, consumer);
	}

	@Override
	public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
		return true;
	}
}

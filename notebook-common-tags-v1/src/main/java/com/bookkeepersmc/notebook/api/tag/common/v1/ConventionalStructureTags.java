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
package com.bookkeepersmc.notebook.api.tag.common.v1;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.gen.feature.StructureFeature;

import com.bookkeepersmc.notebook.impl.tag.common.v1.TagRegistration;

/**
 * See {@link net.minecraft.data.server.tag.StructureTags} for vanilla tags.
 */
public final class ConventionalStructureTags {
	private ConventionalStructureTags() {
	}

	/**
	 * Structures that should not show up on minimaps or world map views from mods/sites.
	 * No effect on vanilla map items.
	 */
	public static final TagKey<StructureFeature> HIDDEN_FROM_DISPLAYERS = register("hidden_from_displayers");

	/**
	 * Structures that should not be locatable/selectable by modded structure-locating items or abilities.
	 * No effect on vanilla map items.
	 */
	public static final TagKey<StructureFeature> HIDDEN_FROM_LOCATOR_SELECTION = register("hidden_from_locator_selection");

	private static TagKey<StructureFeature> register(String tagId) {
		return TagRegistration.STRUCTURE_TAG.registerC(tagId);
	}
}

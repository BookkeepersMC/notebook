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

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

import com.bookkeepersmc.notebook.impl.tag.common.v1.TagRegistration;

/**
 * See {@link net.minecraft.tags.FluidTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 * <p></p>
 * Note, fluid tags should not be plural to match the vanilla standard.
 * This is the only tag category exempted from many-different-types plural rule.
 */
public final class ConventionalFluidTags {
	private ConventionalFluidTags() {
	}

	public static final TagKey<Fluid> LAVA = register("lava");
	public static final TagKey<Fluid> WATER = register("water");
	public static final TagKey<Fluid> MILK = register("milk");
	public static final TagKey<Fluid> HONEY = register("honey");
	/**
	 * Tag that holds all fluids that recipe viewers should not show to users.
	 */
	public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = register("hidden_from_recipe_viewers");

	private static TagKey<Fluid> register(String tagId) {
		return TagRegistration.FLUID_TAG.registerC(tagId);
	}
}

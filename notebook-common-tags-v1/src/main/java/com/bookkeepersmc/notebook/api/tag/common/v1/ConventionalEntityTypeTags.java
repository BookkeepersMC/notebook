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

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import com.bookkeepersmc.notebook.impl.tag.common.v1.TagRegistration;

/**
 * See {@link net.minecraft.tags.EntityTypeTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 */
public final class ConventionalEntityTypeTags {
	private ConventionalEntityTypeTags() {
	}

	/**
	 * Tag containing entity types that display a boss health bar.
	 */
	public static final TagKey<EntityType<?>> BOSSES = register("bosses");

	public static final TagKey<EntityType<?>> MINECARTS = register("minecarts");
	public static final TagKey<EntityType<?>> BOATS = register("boats");

	/**
	 * Entities should be included in this tag if they are not allowed to be picked up by items or grabbed in a way
	 * that a player can easily move the entity to anywhere they want. Ideal for special entities that should not
	 * be able to be put into a mob jar for example.
	 */
	public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = register("capturing_not_supported");

	/**
	 * Entities should be included in this tag if they are not allowed to be teleported in any way.
	 * This is more for mods that allow teleporting entities within the same dimension. Any mod that is
	 * teleporting entities to new dimensions should be checking canUsePortals method on the entity itself.
	 */
	public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = register("teleporting_not_supported");

	private static TagKey<EntityType<?>> register(String tagId) {
		return TagRegistration.ENTITY_TYPE_TAG.registerC(tagId);
	}
}

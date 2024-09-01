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
package com.bookkeepersmc.notebook.impl.recipe.builtin;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Holder;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredient;
import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredientSerializer;

public class DifferenceIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<DifferenceIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	private final Ingredient subtracted;

	public DifferenceIngredient(Ingredient base, Ingredient subtracted) {
		this.base = base;
		this.subtracted = subtracted;
	}

	@Override
	public boolean test(ItemStack stack) {
		return base.test(stack) && !subtracted.test(stack);
	}

	@Override
	public List<Holder<Item>> getMatchingStacks() {
		final List<Holder<Item>> subtractedMatchingStacks = subtracted.method_8105();
		return base.method_8105().stream()
				.filter(registryEntry -> !subtractedMatchingStacks.contains(registryEntry))
				.toList();
	}

	@Override
	public boolean requiresTesting() {
		return base.requiresTesting() || subtracted.requiresTesting();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private Ingredient getBase() {
		return base;
	}

	private Ingredient getSubtracted() {
		return subtracted;
	}

	private static class Serializer implements CustomIngredientSerializer<DifferenceIngredient> {
		private static final Identifier ID = Identifier.of("notebook", "difference");
		private static final MapCodec<DifferenceIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter(DifferenceIngredient::getBase),
						Ingredient.ALLOW_EMPTY_CODEC.fieldOf("subtracted").forGetter(DifferenceIngredient::getSubtracted)
				).apply(instance, DifferenceIngredient::new)
		);
		private static final PacketCodec<RegistryByteBuf, DifferenceIngredient> PACKET_CODEC = PacketCodec.tuple(
				Ingredient.PACKET_CODEC, DifferenceIngredient::getBase,
				Ingredient.PACKET_CODEC, DifferenceIngredient::getSubtracted,
				DifferenceIngredient::new
		);

		@Override
		public Identifier getId() {
			return ID;
		}

		@Override
		public MapCodec<DifferenceIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, DifferenceIngredient> getStreamCodec() {
			return PACKET_CODEC;
		}
	}
}

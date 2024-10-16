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

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.Holder;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredient;
import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredientSerializer;

public class CustomDataIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<CustomDataIngredient> SERIALIZER = new Serializer();
	private final Ingredient base;
	private final NbtCompound nbt;

	public CustomDataIngredient(Ingredient base, NbtCompound nbt) {
		if (nbt == null || nbt.isEmpty()) throw new IllegalArgumentException("NBT cannot be null; use components ingredient for strict matching");

		this.base = base;
		this.nbt = nbt;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (!base.test(stack)) return false;

		NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);

		return nbt != null && nbt.matches(this.nbt);
	}

	@Override
	public List<Holder<Item>> getItems() {
		return base.getItems();
	}

	@Override
	public SlotDisplay getSlotDisplay() {
		return new SlotDisplay.CompositeSlotDisplay(
				base.getItems().stream().map(this::createEntryDisplay).toList()
		);
	}

	private SlotDisplay createEntryDisplay(Holder<Item> entry) {
		ItemStack stack = entry.getValue().getDefaultStack();
		stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, existingNbt -> NbtComponent.of(existingNbt.copy().copyFrom(nbt)));
		return new SlotDisplay.StackSlotDisplay(stack);
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private Ingredient getBase() {
		return base;
	}

	private NbtCompound getNbt() {
		return nbt;
	}

	private static class Serializer implements CustomIngredientSerializer<CustomDataIngredient> {
		private static final Identifier ID = Identifier.of("notebook", "custom_data");

		private static final MapCodec<CustomDataIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter(CustomDataIngredient::getBase),
						StringNbtReader.LENIENT_CODEC.fieldOf("nbt").forGetter(CustomDataIngredient::getNbt)
				).apply(instance, CustomDataIngredient::new)
		);
		private static final PacketCodec<RegistryByteBuf, CustomDataIngredient> PACKET_CODEC = PacketCodec.tuple(
				Ingredient.PACKET_CODEC, CustomDataIngredient::getBase,
				PacketCodecs.NBT_COMPOUND, CustomDataIngredient::getNbt,
				CustomDataIngredient::new
		);

		@Override
		public Identifier getId() {
			return ID;
		}

		@Override
		public MapCodec<CustomDataIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, CustomDataIngredient> getStreamCodec() {
			return PACKET_CODEC;
		}
	}
}

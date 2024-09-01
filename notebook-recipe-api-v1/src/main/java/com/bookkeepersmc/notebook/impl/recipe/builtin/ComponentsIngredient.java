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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.component.DataComponentPatch;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Holder;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredient;
import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredientSerializer;

public class ComponentsIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<ComponentsIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	private final DataComponentPatch components;

	public ComponentsIngredient(Ingredient base, DataComponentPatch components) {
		if (components.isEmpty()) {
			throw new IllegalArgumentException("ComponentIngredient must have at least one defined component");
		}

		this.base = base;
		this.components = components;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (!base.test(stack)) return false;

		// None strict matching
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
			final DataComponentType<?> type = entry.getKey();
			final Optional<?> value = entry.getValue();

			if (value.isPresent()) {
				// Expect the stack to contain a matching component
				if (!stack.contains(type)) {
					return false;
				}

				if (!Objects.equals(value.get(), stack.get(type))) {
					return false;
				}
			} else {
				// Expect the target stack to not contain this component
				if (stack.contains(type)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public List<Holder<Item>> getMatchingStacks() {
		return base.method_8105().stream()
				.filter(itemHolder -> {
					ItemStack stack = itemHolder.value().getDefaultStack();
					stack.applyPatchAndValidate(components);
					return base.test(stack);
				}).toList();
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

	@Nullable
	private DataComponentPatch getComponents() {
		return components;
	}

	private static class Serializer implements CustomIngredientSerializer<ComponentsIngredient> {
		private static final Identifier ID = Identifier.of("notebook", "components");
		private static final MapCodec<ComponentsIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter(ComponentsIngredient::getBase),
					DataComponentPatch.CODEC.fieldOf("components").forGetter(ComponentsIngredient::getComponents)
			).apply(instance, ComponentsIngredient::new)
		);
		private static final PacketCodec<RegistryByteBuf, ComponentsIngredient> PACKET_CODEC = PacketCodec.tuple(
				Ingredient.PACKET_CODEC, ComponentsIngredient::getBase,
				DataComponentPatch.PACKET_CODEC, ComponentsIngredient::getComponents,
				ComponentsIngredient::new
		);

		@Override
		public Identifier getId() {
			return ID;
		}

		@Override
		public MapCodec<ComponentsIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, ComponentsIngredient> getStreamCodec() {
			return PACKET_CODEC;
		}
	}
}

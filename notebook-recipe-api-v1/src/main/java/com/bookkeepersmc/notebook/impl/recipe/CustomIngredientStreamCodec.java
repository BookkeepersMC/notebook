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
package com.bookkeepersmc.notebook.impl.recipe;

import java.util.Set;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredient;
import com.bookkeepersmc.notebook.api.recipe.v1.CustomIngredientSerializer;

public class CustomIngredientStreamCodec implements PacketCodec<RegistryByteBuf, Ingredient> {
	private static final int PACKET_MARKER = -1;
	private final PacketCodec<RegistryByteBuf, Ingredient> fallback;

	public CustomIngredientStreamCodec(PacketCodec<RegistryByteBuf, Ingredient> fallback) {
		this.fallback = fallback;
	}

	@Override
	public Ingredient decode(RegistryByteBuf buf) {
		int index = buf.readerIndex();

		if (buf.readVarInt() != PACKET_MARKER) {
			// Reset index for vanilla's normal deserialization logic.
			buf.readerIndex(index);
			return this.fallback.decode(buf);
		}

		Identifier type = buf.readIdentifier();
		CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(type);

		if (serializer == null) {
			throw new IllegalArgumentException("Cannot deserialize custom ingredient of unknown type " + type);
		}

		return serializer.getStreamCodec().decode(buf).toVanilla();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void encode(RegistryByteBuf buf, Ingredient value) {
		CustomIngredient customIngredient = value.getCustomIngredient();

		if (shouldEncodeFallback(customIngredient)) {
			this.fallback.encode(buf, value);
			return;
		}

		buf.writeVarInt(PACKET_MARKER);
		buf.writeIdentifier(customIngredient.getSerializer().getId());
		PacketCodec<RegistryByteBuf, CustomIngredient> packetCodec = (PacketCodec<RegistryByteBuf, CustomIngredient>) customIngredient.getSerializer().getStreamCodec();
		packetCodec.encode(buf, customIngredient);
	}

	private static boolean shouldEncodeFallback(CustomIngredient customIngredient) {
		if (customIngredient == null) {
			return true;
		}

		Set<Identifier> supportedIngredients = CustomIngredientSync.CURRENT_SUPPORTED_INGREDIENTS.get();
		return supportedIngredients != null && !supportedIngredients.contains(customIngredient.getSerializer().getId());
	}
}

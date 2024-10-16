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
package com.bookkeepersmc.notebook.api.resource.conditions.v1;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;


public interface ResourceConditionType<T extends ResourceCondition> {

	Codec<ResourceConditionType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id ->
					CustomNull.mapOrDefault(ResourceConditions.getConditionType(id), DataResult::success, () -> DataResult.error(() -> "Unknown resource condition key: "+ id)),
			ResourceConditionType::id
	);

	Identifier id();

	MapCodec<T> codec();

	static <T extends ResourceCondition> ResourceConditionType<T> create(Identifier id, MapCodec<T> codec) {
		Objects.requireNonNull(id, "id cannot be null");
		Objects.requireNonNull(codec, "codec cannot be null");

		return new ResourceConditionType<>() {
			@Override
			public Identifier id() {
				return id;
			}

			@Override
			public MapCodec<T> codec() {
				return codec;
			}
		};
	}

	class CustomNull {
		// WHY DOES THE OTHER ONE NOT WORK??????????????????
		public static <T, R> R mapOrDefault(@Nullable T value, Function<T, R> func, Supplier<R> rSupplier) {
			return value == null ? rSupplier.get() : func.apply(value);
		}
	}
}

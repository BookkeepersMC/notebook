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
package com.bookkeepersmc.notebook.impl.mod.screen.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public final class EnumToLowerCaseJsonConverter implements JsonSerializer<Enum<?>>, JsonDeserializer<Enum<?>> {
	private static final Map<String, Class<? extends Enum<?>>> TYPE_CACHE = new HashMap<>();

	@Override
	public JsonElement serialize(final Enum<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
		if (src == null) {
			return JsonNull.INSTANCE;
		}
		return new JsonPrimitive(src.name().toLowerCase());
	}

	@Override
	public Enum<?> deserialize(
		final JsonElement json,
		final Type type,
		final JsonDeserializationContext context
	) throws JsonParseException {
		if (json == null || json.isJsonNull()) {
			return null;
		}

		if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
			throw new JsonParseException("Expecting a String JsonPrimitive, getting " + json);
		}

		try {
			final String enumClassName = type.getTypeName();
			Class<? extends Enum<?>> enumClass = TYPE_CACHE.get(enumClassName);
			if (enumClass == null) {
				enumClass = (Class<? extends Enum<?>>) Class.forName(enumClassName);
				TYPE_CACHE.put(enumClassName, enumClass);
			}

			return Enum.valueOf((Class) enumClass, json.getAsString().toUpperCase());
		} catch (final ClassNotFoundException e) {
			throw new JsonParseException(e);
		}
	}
}

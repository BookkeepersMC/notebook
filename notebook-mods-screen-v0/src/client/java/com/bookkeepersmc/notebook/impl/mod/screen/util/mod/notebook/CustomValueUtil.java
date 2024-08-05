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
package com.bookkeepersmc.notebook.impl.mod.screen.util.mod.notebook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.bookkeepersmc.loader.api.metadata.CustomValue;
import com.bookkeepersmc.loader.api.metadata.ModMetadata;

public class CustomValueUtil {
	public static Optional<Boolean> getBoolean(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return Optional.of(metadata.getCustomValue(key).getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<String> getString(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return Optional.of(metadata.getCustomValue(key).getAsString());
		}
		return Optional.empty();
	}

	public static Optional<Set<String>> getStringSet(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return getStringSet(key, metadata.getCustomValue(key).getAsObject());
		}
		return Optional.empty();
	}

	public static Optional<Boolean> getBoolean(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			return Optional.of(object.get(key).getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<String> getString(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			return Optional.of(object.get(key).getAsString());
		}
		return Optional.empty();
	}

	public static Optional<String[]> getStringArray(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			CustomValue.CvArray cvArray = object.get(key).getAsArray();
			String[] strings = new String[cvArray.size()];
			for (int i = 0; i < cvArray.size(); i++) {
				strings[i] = cvArray.get(i).getAsString();
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}

	public static Optional<Set<String>> getStringSet(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			Set<String> strings = new HashSet<>();
			for (CustomValue value : object.get(key).getAsArray()) {
				strings.add(value.getAsString());
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}

	public static Optional<Map<String, String>> getStringMap(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			Map<String, String> strings = new HashMap<>();
			for (Map.Entry<String, CustomValue> entry : object.get(key).getAsObject()) {
				strings.put(entry.getKey(), entry.getValue().getAsString());
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}
}

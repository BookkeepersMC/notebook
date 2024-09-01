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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resource.pack.metadata.MetadataSectionType;

public record OverlayConditionsMetadata(List<Entry> overlays) {
	public static final Codec<OverlayConditionsMetadata> CODEC = Entry.CODEC.listOf().fieldOf("entries").xmap(OverlayConditionsMetadata::new, OverlayConditionsMetadata::overlays).codec();
	public static final MetadataSectionType<OverlayConditionsMetadata> SERIALIZER = MetadataSectionType.fromCodec(ResourceConditions.OVERLAYS_KEY, CODEC);

	public List<String> appliedOverlays() {
		List<String> appliedOverlays = new ArrayList<>();

		for (Entry entry : this.overlays()) {
			if (entry.resourceCondition().test(null)) {
				appliedOverlays.add(entry.directory());
			}
		}

		return appliedOverlays;
	}

	public record Entry(String directory, ResourceCondition resourceCondition) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.validate(Entry::validateDirectory).fieldOf("directory").forGetter(Entry::directory),
				ResourceCondition.CODEC.fieldOf("condition").forGetter(Entry::resourceCondition)
		).apply(instance, Entry::new));

		private static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("[-_a-zA-Z0-9.]+");

		private static DataResult<String> validateDirectory(String directory) {
			boolean valid = DIRECTORY_NAME_PATTERN.matcher(directory).matches();
			return valid ? DataResult.success(directory) : DataResult.error(() -> "Directory name is invalid");
		}
	}
}

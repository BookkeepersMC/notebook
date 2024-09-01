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
package com.bookkeepersmc.notebook.mixin.resource.loader.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.util.Language;

import com.bookkeepersmc.notebook.impl.resource.loader.ServerLanguageUtil;

@Mixin(Language.class)
class LanguageMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Redirect(method = "create", at = @At(value = "INVOKE", target = "Ljava/util/Map;copyOf(Ljava/util/Map;)Ljava/util/Map;", remap = false))
	private static Map<String, String> create(Map<String, String> map) {

		for (Path path : ServerLanguageUtil.getModLanguageFiles()) {
			loadFromPath(path, map::put);
		}

		return ImmutableMap.copyOf(map);
	}

	private static void loadFromPath(Path path, BiConsumer<String, String> entryConsumer) {
		try (InputStream stream = Files.newInputStream(path)) {
			LOGGER.debug("Loading translations from {}", path);
			load(stream, entryConsumer);
		} catch (JsonParseException | IOException e) {
			LOGGER.error("Couldn't read strings from {}", path, e);
		}
	}

	@Shadow
	public static void load(InputStream inputStream, BiConsumer<String, String> entryConsumer) {
	}
}

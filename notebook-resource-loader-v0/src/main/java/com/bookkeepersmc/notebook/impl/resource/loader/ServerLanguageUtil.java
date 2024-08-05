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
package com.bookkeepersmc.notebook.impl.resource.loader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackType;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;

public final class ServerLanguageUtil {
	private static final String ASSETS_PREFIX = PackType.CLIENT_RESOURCES.getDirectory() + '/';

	private ServerLanguageUtil() {
	}

	public static Collection<Path> getModLanguageFiles() {
		Set<Path> paths = new LinkedHashSet<>();

		for (ModContainer mod : NotebookLoader.getInstance().getAllMods()) {
			if (mod.getMetadata().getType().equals("builtin")) continue;

			final Map<PackType, Set<String>> map = ModNioResourcePack.readNamespaces(mod.getRootPaths(), mod.getMetadata().getId());

			for (String ns : map.get(PackType.CLIENT_RESOURCES)) {
				mod.findPath(ASSETS_PREFIX + ns + "/lang/" + Language.DEFAULT + ".json")
						.filter(Files::isRegularFile)
						.ifPresent(paths::add);
			}
		}

		return Collections.unmodifiableCollection(paths);
	}
}

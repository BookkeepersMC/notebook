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
package com.bookkeepersmc.notebook.impl.client.rendering;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.loader.api.metadata.ModMetadata;

public class RendererMixinConfigPlugin implements IMixinConfigPlugin {
	/** Set by other renderers to disable loading of Indigo. */
	private static final String JSON_KEY_DISABLE_INDIGO = "notebook-renderer-api-v1:contains_renderer";
	/**
	 * Disables vanilla block tesselation and ensures vertex format compatibility.
	 */
	private static final String JSON_KEY_FORCE_COMPATIBILITY = "notebook-renderer:force_compatibility";

	private static boolean needsLoad = true;

	private static boolean indigoApplicable = true;
	private static boolean forceCompatibility = false;

	private static void loadIfNeeded() {
		if (needsLoad) {
			for (ModContainer container : NotebookLoader.getInstance().getAllMods()) {
				final ModMetadata meta = container.getMetadata();

				if (meta.containsCustomValue(JSON_KEY_DISABLE_INDIGO)) {
					indigoApplicable = false;
				} else if (meta.containsCustomValue(JSON_KEY_FORCE_COMPATIBILITY)) {
					forceCompatibility = true;
				}
			}

			needsLoad = false;
		}
	}

	static boolean shouldApplyIndigo() {
		loadIfNeeded();
		return indigoApplicable;
	}

	static boolean shouldForceCompatibility() {
		loadIfNeeded();
		return forceCompatibility;
	}

	@Override
	public void onLoad(String mixinPackage) { }

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return shouldApplyIndigo();
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}

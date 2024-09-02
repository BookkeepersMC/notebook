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
package com.bookkeepersmc.notebook.mixin.crash.details;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.SystemDetails;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;

@Mixin(SystemDetails.class)
public abstract class SystemReportMixin {
	@Shadow
	public abstract void addSection(String string, Supplier<String> supplier);

	@Inject(at = @At("RETURN"), method = "<init>")
	private void fillSystemDetails(CallbackInfo info) {
		addSection("Loaded Mods", () -> {
			ArrayList<ModContainer> topLevelMods = new ArrayList<>();

			for (ModContainer container : NotebookLoader.getInstance().getAllMods()) {
				if (container.getContainingMod().isEmpty()) {
					topLevelMods.add(container);
				}
			}

			StringBuilder modString = new StringBuilder();

			appendMods(modString, 2, topLevelMods);

			return modString.toString();
		});
	}

	private static void appendMods(StringBuilder modString, int depth, ArrayList<ModContainer> mods) {
		mods.sort(Comparator.comparing(mod -> mod.getMetadata().getId()));

		for (ModContainer mod : mods) {
			modString.append('\n');
			modString.append("\t".repeat(depth));
			modString.append(mod.getMetadata().getId());
			modString.append(": ");
			modString.append(mod.getMetadata().getName());
			modString.append(' ');
			modString.append(mod.getMetadata().getVersion().getFriendlyString());

			if (!mod.getContainedMods().isEmpty()) {
				ArrayList<ModContainer> childMods = new ArrayList<>(mod.getContainedMods());
				appendMods(modString, depth + 1, childMods);
			}
		}
	}
}

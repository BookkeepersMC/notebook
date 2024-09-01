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
package com.bookkeepersmc.notebook.mixin.resource.loader.client;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.Minecraft;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.resource.pack.PackProfile;

import com.bookkeepersmc.notebook.impl.resource.loader.NotebookResourcePackProfile;

/**
 * Mixins to the anonymous class in #write method.
 */
@Mixin(targets = "net/minecraft/client/option/GameOptions$C_runhizzk")
public class GameOptionsWriteVisitorMixin {
	@Unique
	private static List<String> toPackListString(List<String> packs) {
		List<String> copy = new ArrayList<>(packs.size());
		PackManager manager = Minecraft.getInstance().getResourcePackManager();

		for (String pack : packs) {
			PackProfile profile = manager.getProfile(pack);

			// Nonexistent pack profiles should be handled in the same way as vanilla
			if (profile == null || !((NotebookResourcePackProfile) profile).notebook_isHidden()) copy.add(pack);
		}

		return copy;
	}

	@SuppressWarnings("unchecked")
	@ModifyArg(method = "visitObject(Ljava/lang/String;Ljava/lang/Object;Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
	private <T> T skipHiddenPacks(T value, @Local String key) {
		if ("resourcePacks".equals(key) && value instanceof List) {
			return (T) toPackListString((List<String>) value);
		}

		return value;
	}
}

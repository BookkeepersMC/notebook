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
package com.bookkeepersmc.notebook.mixin.resource.conditions;

import java.util.Iterator;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import com.bookkeepersmc.notebook.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(JsonDataLoader.class)
public class SimpleJsonResourceReloadListenerMixin extends SimplePreparableReloadListenerMixin {
	@Shadow
	@Final
	private String dataType;

	@Override
	@SuppressWarnings("unchecked")
	protected void notebook_applyResourceConditions(ResourceManager resourceManager, Profiler profiler, Object object, @Nullable RegistryOps.RegistryInfoLookup registryLookup) {
		profiler.push("Notebook resource conditions: %s".formatted(dataType));

		Iterator<Map.Entry<Identifier, JsonElement>> it = ((Map<Identifier, JsonElement>) object).entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Identifier, JsonElement> entry = it.next();

			JsonElement resourceData = entry.getValue();

			if (resourceData.isJsonObject()) {
				JsonObject obj = resourceData.getAsJsonObject();

				if (!ResourceConditionsImpl.applyResourceConditions(obj, dataType, entry.getKey(), notebook_getRegistryLookup())) {
					it.remove();
				}
			}
		}

		profiler.pop();
	}
}

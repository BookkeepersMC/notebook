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
package com.bookkeepersmc.notebook.impl.registry.sync;

import java.util.LinkedHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class RegistryMapSerializer {
	public static final int VERSION = 1;

	public static Map<ResourceLocation, Object2IntMap<ResourceLocation>> fromNbt(CompoundTag nbt) {
		CompoundTag mainNbt = nbt.getCompound("registries");
		Map<ResourceLocation, Object2IntMap<ResourceLocation>> map = new LinkedHashMap<>();

		for (String registryId : mainNbt.getAllKeys()) {
			Object2IntMap<ResourceLocation> idMap = new Object2IntLinkedOpenHashMap<>();
			CompoundTag idNbt = mainNbt.getCompound(registryId);

			for (String id : idNbt.getAllKeys()) {
				idMap.put(ResourceLocation.parse(id), idNbt.getInt(id));
			}

			map.put(ResourceLocation.parse(registryId), idMap);
		}

		return map;
	}

	public static CompoundTag toNbt(Map<ResourceLocation, Object2IntMap<ResourceLocation>> map) {
		CompoundTag mainNbt = new CompoundTag();

		map.forEach((registryId, idMap) -> {
			CompoundTag registryNbt = new CompoundTag();

			for (Object2IntMap.Entry<ResourceLocation> idPair : idMap.object2IntEntrySet()) {
				registryNbt.putInt(idPair.getKey().toString(), idPair.getIntValue());
			}

			mainNbt.put(registryId.toString(), registryNbt);
		});

		CompoundTag nbt = new CompoundTag();
		nbt.putInt("version", VERSION);
		nbt.put("registries", mainNbt);
		return nbt;
	}
}

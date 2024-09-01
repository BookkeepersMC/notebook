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

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class RegistryMapSerializer {
	public static final int VERSION = 1;

	public static Map<Identifier, Object2IntMap<Identifier>> fromNbt(NbtCompound nbt) {
		NbtCompound mainNbt = nbt.getCompound("registries");
		Map<Identifier, Object2IntMap<Identifier>> map = new LinkedHashMap<>();

		for (String registryId : mainNbt.getKeys()) {
			Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
			NbtCompound idNbt = mainNbt.getCompound(registryId);

			for (String id : idNbt.getKeys()) {
				idMap.put(Identifier.parse(id), idNbt.getInt(id));
			}

			map.put(Identifier.parse(registryId), idMap);
		}

		return map;
	}

	public static NbtCompound toNbt(Map<Identifier, Object2IntMap<Identifier>> map) {
		NbtCompound mainNbt = new NbtCompound();

		map.forEach((registryId, idMap) -> {
			NbtCompound registryNbt = new NbtCompound();

			for (Object2IntMap.Entry<Identifier> idPair : idMap.object2IntEntrySet()) {
				registryNbt.putInt(idPair.getKey().toString(), idPair.getIntValue());
			}

			mainNbt.put(registryId.toString(), registryNbt);
		});

		NbtCompound nbt = new NbtCompound();
		nbt.putInt("version", VERSION);
		nbt.put("registries", mainNbt);
		return nbt;
	}
}

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
package com.bookkeepersmc.notebook.api.client.keybind.v1;

import java.util.Objects;

import com.mojang.blaze3d.platform.InputUtil;

import net.minecraft.client.option.KeyBind;

import com.bookkeepersmc.notebook.impl.client.keybinding.KeybindingRegistryImpl;
import com.bookkeepersmc.notebook.mixin.client.keybind.KeyMappingAccessor;

public final class KeybindHelper {
	private KeybindHelper() {
	}

	public static KeyBind registerKeybind(KeyBind mapping) {
		Objects.requireNonNull(mapping, "Key binding cannot be null!");
		return KeybindingRegistryImpl.registerKeybind(mapping);
	}

	public static InputUtil.Key getBoundKeyOf(KeyBind mapping) {
		return ((KeyMappingAccessor) mapping).notebook_getKey();
	}
}

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
package com.bookkeepersmc.notebook.api.datagen.v1;

import java.nio.file.Path;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.data.PackOutput;

import com.bookkeepersmc.loader.api.ModContainer;

public final class NotebookDataOutput extends PackOutput {
	private final ModContainer modContainer;
	private final boolean strictValidation;

	@ApiStatus.Internal
	public NotebookDataOutput(ModContainer modContainer, Path path, boolean strictValidation) {
		super(path);
		this.modContainer = modContainer;
		this.strictValidation = strictValidation;
	}

	public ModContainer getModContainer() {
		return modContainer;
	}

	public String getModId() {
		return getModContainer().getMetadata().getId();
	}

	public boolean isStrictValidationEnabled() {
		return strictValidation;
	}
}

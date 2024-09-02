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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.registry.HolderLookup;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.loader.api.ModContainer;

public final class NotebookDataGenerator extends DataGenerator {
	private final ModContainer modContainer;
	private final boolean strictValidation;
	private final NotebookDataOutput notebookOutput;
	private final CompletableFuture<HolderLookup.Provider> provider;

	public NotebookDataGenerator(Path output, ModContainer mod, boolean strictValidation, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, SharedConstants.getGameVersion(), true);
		this.modContainer = Objects.requireNonNull(mod);
		this.strictValidation = strictValidation;
		this.notebookOutput = new NotebookDataOutput(mod, output, strictValidation);
		this.provider = registriesFuture;
	}

	public Pack createPack() {
		return new Pack(true, modContainer.getMetadata().getName(), this.notebookOutput);
	}

	public Pack createBuiltinResourcePack(Identifier id) {
		Path path = this.output.getPath().resolve("resourcepacks").resolve(id.getPath());
		return new Pack(true, id.toString(), new NotebookDataOutput(modContainer, path, strictValidation));
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

	public CompletableFuture<HolderLookup.Provider> getRegistries() {
		return provider;
	}

	@Override
	@Deprecated
	public PackGenerator createVanillaPack(boolean bl) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public PackGenerator createDataPack(boolean bl, String string) {
		throw new UnsupportedOperationException();
	}

	public final class Pack extends DataGenerator.PackGenerator {
		private Pack(boolean shouldRun, String name, NotebookDataOutput output) {
			super(shouldRun, name, output);
		}

		public <T extends DataProvider> T addProvider(Factory<T> factory) {
			return super.addProvider(packOutput -> factory.create((NotebookDataOutput) packOutput));
		}

		public <T extends DataProvider> T addProvider(RegistryDependentFactory<T> factory) {
			return super.addProvider(packOutput -> factory.create((NotebookDataOutput) packOutput, provider));
		}

		@FunctionalInterface
		public interface Factory<T extends DataProvider> {
			T create(NotebookDataOutput output);
		}

		@FunctionalInterface
		public interface RegistryDependentFactory<T extends DataProvider> {
			T create(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> provider);
		}
	}
}

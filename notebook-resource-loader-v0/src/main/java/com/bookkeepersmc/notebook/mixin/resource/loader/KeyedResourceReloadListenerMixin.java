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
package com.bookkeepersmc.notebook.mixin.resource.loader;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.resource.IdentifiableResourceReloadListener;
import com.bookkeepersmc.notebook.api.resource.ResourceReloadListenerKeys;

@Mixin({
		/* public */
		RecipeManager.class, ServerAdvancementLoader.class, FunctionLoader.class
		/* private */
})
public abstract class KeyedResourceReloadListenerMixin implements IdentifiableResourceReloadListener {
	private Identifier notebook$id;
	private Collection<Identifier> notebook$dependencies;

	@Override
	@SuppressWarnings({"ConstantConditions", "RedundantCast"})
	public Identifier getNotebookId() {
		if (this.notebook$id == null) {
			Object self = this;

			if (self instanceof RecipeManager) {
				this.notebook$id = ResourceReloadListenerKeys.RECIPES;
			} else if (self instanceof ServerAdvancementLoader) {
				this.notebook$id = ResourceReloadListenerKeys.ADVANCEMENTS;
			} else if (self instanceof FunctionLoader) {
				this.notebook$id = ResourceReloadListenerKeys.FUNCTIONS;
			} else {
				this.notebook$id = Identifier.ofDefault("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.notebook$id;
	}

	@Override
	@SuppressWarnings({"ConstantConditions", "RedundantCast"})
	public Collection<Identifier> getNotebookDependencies() {
		if (this.notebook$dependencies == null) {
			this.notebook$dependencies = Collections.emptyList();
		}

		return notebook$dependencies;
	}
}

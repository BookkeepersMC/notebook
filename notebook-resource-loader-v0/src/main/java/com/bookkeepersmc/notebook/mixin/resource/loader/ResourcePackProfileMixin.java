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

import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.pack.PackLocationInfo;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.ResourcePack;

import com.bookkeepersmc.notebook.impl.resource.loader.NotebookResourcePackProfile;
import com.bookkeepersmc.notebook.impl.resource.loader.ResourcePackSourceTracker;

/**
 * Implements resource pack source tracking (for {@link com.bookkeepersmc.notebook.impl.resource.loader.NotebookResource}).
 * {@link PackResources} doesn't hold a reference to its {@link PackSource}
 * so we store the source in a global tracker when the resource packs are created.
 *
 * @see ResourcePackSourceTracker
 */
@Mixin(PackProfile.class)
abstract class ResourcePackProfileMixin implements NotebookResourcePackProfile {
	@Unique
	private static final Predicate<Set<String>> DEFAULT_PARENT_PREDICATE = parents -> true;
	@Unique
	private Predicate<Set<String>> parentsPredicate = DEFAULT_PARENT_PREDICATE;

	@Shadow
	public abstract PackLocationInfo getPackLocationInfo();

	@Inject(method = "createPack", at = @At("RETURN"))
	private void onCreateResourcePack(CallbackInfoReturnable<ResourcePack> info) {
		ResourcePackSourceTracker.setSource(info.getReturnValue(), getPackLocationInfo().source());
	}

	@Override
	public boolean notebook_isHidden() {
		return parentsPredicate != DEFAULT_PARENT_PREDICATE;
	}

	@Override
	public boolean notebook_parentsEnabled(Set<String> enabled) {
		return parentsPredicate.test(enabled);
	}

	@Override
	public void notebook_setParentsPredicate(Predicate<Set<String>> predicate) {
		this.parentsPredicate = predicate;
	}
}

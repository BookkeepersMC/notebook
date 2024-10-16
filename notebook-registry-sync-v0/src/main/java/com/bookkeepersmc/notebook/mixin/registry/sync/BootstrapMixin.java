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
package com.bookkeepersmc.notebook.mixin.registry.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.registry.BuiltInRegistries;

import com.bookkeepersmc.notebook.impl.registry.sync.RegistrySyncManager;
import com.bookkeepersmc.notebook.impl.registry.sync.trackers.StateIdTracker;
import com.bookkeepersmc.notebook.impl.registry.sync.trackers.vanilla.BlockItemTracker;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
	@Inject(method = "setOutputStreams", at = @At("RETURN"))
	private static void initialize(CallbackInfo info) {
		// These seemingly pointless accesses are done to make sure each
		// static initializer is called, to register vanilla-provided blocks
		// and items from the respective classes - otherwise, they would
		// duplicate our calls from below.
		Object oBlock = Blocks.AIR;
		Object oFluid = Fluids.EMPTY;
		Object oItem = Items.AIR;

		// state ID tracking
		StateIdTracker.register(BuiltInRegistries.BLOCK, Block.STATE_IDS, (block) -> block.getStateManager().getStates());
		StateIdTracker.register(BuiltInRegistries.FLUID, Fluid.STATE_IDS, (fluid) -> fluid.getStateManager().getStates());

		// map tracking
		BlockItemTracker.register(BuiltInRegistries.ITEM);

		RegistrySyncManager.bootstrapRegistries();
	}

	@Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/BuiltInRegistries;bootstrap()V"))
	private static void bootStrap() {
		BuiltInRegistries.createContents();
	}
}

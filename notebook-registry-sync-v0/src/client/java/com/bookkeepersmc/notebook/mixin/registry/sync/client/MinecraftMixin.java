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
package com.bookkeepersmc.notebook.mixin.registry.sync.client;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.BuiltInRegistries;

import com.bookkeepersmc.notebook.impl.registry.sync.RegistrySyncManager;
import com.bookkeepersmc.notebook.impl.registry.sync.RemapException;
import com.bookkeepersmc.notebook.impl.registry.sync.trackers.vanilla.BlockInitTracker;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Inject(at = @At("RETURN"), method = "method_18096")
	public void disconnectAfter(Screen disconnectionScreen, boolean bl, CallbackInfo ci) {
		try {
			RegistrySyncManager.unmap();
		} catch (RemapException e) {
			LOGGER.warn("Failed to unmap Notebook registries!", e);
		}
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"))
	private void afterModInit(CallbackInfo ci) {
		// Freeze the registries on the client
		LOGGER.debug("Freezing registries");
		BuiltInRegistries.bootstrap();
		BlockInitTracker.postFreeze();
		ItemGroups.bootstrap();
	}
}

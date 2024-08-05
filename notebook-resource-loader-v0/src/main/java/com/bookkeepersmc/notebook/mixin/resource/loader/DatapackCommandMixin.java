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
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

import com.bookkeepersmc.notebook.impl.resource.loader.NotebookResourcePackProfile;

/**
 * Disables enabling/disabling internal data packs.
 * Listing them is still allowed, but they do not appear in suggestions.
 */
@Mixin(DataPackCommand.class)
public class DatapackCommandMixin {
	@Unique
	private static final DynamicCommandExceptionType INTERNAL_PACK_EXCEPTION = new DynamicCommandExceptionType(
			packName -> Component.translatableEscape("commands.datapack.notebook.internal", packName));

	@Redirect(method = "method_13136", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;getSelectedIds()Ljava/util/Collection;"))
	private static Collection<String> filterEnabledPackSuggestions(PackRepository dataPackManager) {
		return dataPackManager.getSelectedPacks().stream().filter(profile -> !((NotebookResourcePackProfile) profile).notebook_isHidden()).map(Pack::getId).toList();
	}

	@WrapOperation(method = "method_13120", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0))
	private static Stream<Pack> filterDisabledPackSuggestions(Stream<Pack> instance, Predicate<? super Pack> predicate, Operation<Stream<Pack>> original) {
		return original.call(instance, predicate).filter(profile -> !((NotebookResourcePackProfile) profile).notebook_isHidden());
	}

	@Inject(method = "getPack", at = @At(value = "INVOKE", target = "Ljava/util/Collection;contains(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE))
	private static void errorOnInternalPack(CommandContext<CommandSourceStack> context, String name, boolean enable, CallbackInfoReturnable<Pack> cir, @Local Pack profile) throws CommandSyntaxException {
		if (((NotebookResourcePackProfile) profile).notebook_isHidden()) throw INTERNAL_PACK_EXCEPTION.create(profile.getId());
	}
}

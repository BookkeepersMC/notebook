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
package com.bookkeepersmc.notebook.impl.mod.screen.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ModScreenTexts {
	public static final Component CONFIGURE = Component.translatable("modscreen.configure");
	public static final Component DROP_CONFIRM = Component.translatable("modscreen.dropConfirm");
	public static final Component DROP_INFO_LINE_1 = Component.translatable("modscreen.dropInfo.line1");
	public static final Component DROP_INFO_LINE_2 = Component.translatable("modscreen.dropInfo.line2");
	public static final Component DROP_SUCCESSFUL_LINE_1 = Component.translatable("modscreen.dropSuccessful.line1");
	public static final Component DROP_SUCCESSFUL_LINE_2 = Component.translatable("modscreen.dropSuccessful.line2");
	public static final Component ISSUES = Component.translatable("modscreen.issues");
	public static final Component MODS_FOLDER = Component.translatable("modscreen.modsFolder");
	public static final Component SEARCH = Component.translatable("modscreen.search");
	public static final Component TITLE = Component.translatable("modscreen.title");
	public static final Component TOGGLE_FILTER_OPTIONS = Component.translatable("modscreen.toggleFilterOptions");
	public static final Component WEBSITE = Component.translatable("modscreen.website");

	private ModScreenTexts() {
	}

	public static Component modIdTooltip(String modId) {
		return Component.translatable("modscreen.modIdToolTip", modId);
	}

	public static Component configureError(String modId, Throwable e) {
		return Component.translatable("modscreen.configure.error", modId, modId)
			.append(CommonComponents.NEW_LINE)
			.append(CommonComponents.NEW_LINE)
			.append(e.toString())
			.withStyle(ChatFormatting.RED);
	}
}

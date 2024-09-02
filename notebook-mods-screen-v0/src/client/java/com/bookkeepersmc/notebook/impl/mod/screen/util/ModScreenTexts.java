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

import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ModScreenTexts {
	public static final Text CONFIGURE = Text.translatable("modscreen.configure");
	public static final Text DROP_CONFIRM = Text.translatable("modscreen.dropConfirm");
	public static final Text DROP_INFO_LINE_1 = Text.translatable("modscreen.dropInfo.line1");
	public static final Text DROP_INFO_LINE_2 = Text.translatable("modscreen.dropInfo.line2");
	public static final Text DROP_SUCCESSFUL_LINE_1 = Text.translatable("modscreen.dropSuccessful.line1");
	public static final Text DROP_SUCCESSFUL_LINE_2 = Text.translatable("modscreen.dropSuccessful.line2");
	public static final Text ISSUES = Text.translatable("modscreen.issues");
	public static final Text MODS_FOLDER = Text.translatable("modscreen.modsFolder");
	public static final Text SEARCH = Text.translatable("modscreen.search");
	public static final Text TITLE = Text.translatable("modscreen.title");
	public static final Text TOGGLE_FILTER_OPTIONS = Text.translatable("modscreen.toggleFilterOptions");
	public static final Text WEBSITE = Text.translatable("modscreen.website");

	private ModScreenTexts() {
	}

	public static Text modIdTooltip(String modId) {
		return Text.translatable("modscreen.modIdToolTip", modId);
	}

	public static Text configureError(String modId, Throwable e) {
		return Text.translatable("modscreen.configure.error", modId, modId)
			.append(CommonTexts.LINE_BREAK)
			.append(CommonTexts.LINE_BREAK)
			.append(e.toString())
			.formatted(Formatting.RED);
	}
}

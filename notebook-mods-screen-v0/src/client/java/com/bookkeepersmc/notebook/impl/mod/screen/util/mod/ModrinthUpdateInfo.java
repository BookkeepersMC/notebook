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
package com.bookkeepersmc.notebook.impl.mod.screen.util.mod;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Text;

import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateChannel;
import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateInfo;
import com.bookkeepersmc.notebook.impl.mod.screen.util.VersionUtil;

public class ModrinthUpdateInfo implements UpdateInfo {
	protected final String projectId;
	protected final String versionId;
	protected final String versionNumber;
	protected final UpdateChannel updateChannel;

	private static final Text MODRINTH_TEXT = Text.translatable("modscreen.modrinth");

	public ModrinthUpdateInfo(String projectId, String versionId, String versionNumber, UpdateChannel updateChannel) {
		this.projectId = projectId;
		this.versionId = versionId;
		this.versionNumber = versionNumber;
		this.updateChannel = updateChannel;
	}

	@Override
	public boolean isUpdateAvailable() {
		return true;
	}

	@Override
	public @Nullable Text getUpdateMessage() {
		return Text.translatable("modscreen.updateText", VersionUtil.stripPrefix(this.versionNumber), MODRINTH_TEXT);
	}

	@Override
	public String getDownloadLink() {
		return "https://modrinth.com/project/%s/version/%s".formatted(projectId, versionId);
	}

	public String getProjectId() {
		return projectId;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	@Override
	public UpdateChannel getUpdateChannel() {
		return this.updateChannel;
	}
}

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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import net.minecraft.SharedConstants;

import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;

public class HttpUtil {
	private static final String USER_AGENT = buildUserAgent();
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	private HttpUtil() {
	}

	public static <T> HttpResponse<T> request(
		HttpRequest.Builder builder,
		HttpResponse.BodyHandler<T> handler
	) throws IOException, InterruptedException {
		builder.setHeader("User-Agent", USER_AGENT);
		return HTTP_CLIENT.send(builder.build(), handler);
	}

	private static String buildUserAgent() {
		String env = NotebookModScreen.DEV_ENVIRONMENT ? "/development" : "";

		var modMenuVersion = getModMenuVersion();
		var minecraftVersion = SharedConstants.getCurrentVersion().getName();

		// -> TerraformersMC/ModMenu/9.1.0 (1.20.3/quilt/development)
		return "%s (%s/%s%s)".formatted(modMenuVersion, minecraftVersion, "fabric", env);
	}

	private static String getModMenuVersion() {
		var container = NotebookLoader.getInstance().getModContainer(NotebookModScreen.MOD_ID);

		if (container.isEmpty()) {
			throw new RuntimeException("Unable to find Mod Screen's own mod container!");
		}

		return VersionUtil.removeBuildMetadata(container.get().getMetadata().getVersion().getFriendlyString());
	}
}

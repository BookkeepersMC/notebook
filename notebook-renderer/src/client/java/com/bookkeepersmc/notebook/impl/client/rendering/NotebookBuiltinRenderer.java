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
package com.bookkeepersmc.notebook.impl.client.rendering;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bookkeepersmc.api.ClientModInitializer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.notebook.api.renderer.v1.RendererAccess;
import com.bookkeepersmc.notebook.api.util.TriState;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.NotebookRenderer;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.aocalc.AoConfig;

public class NotebookBuiltinRenderer implements ClientModInitializer {
	public static final boolean ALWAYS_TESSELATE_INDIGO;
	public static final boolean ENSURE_VERTEX_FORMAT_COMPATIBILITY;
	public static final AoConfig AMBIENT_OCCLUSION_MODE;
	/** Set true in dev env to confirm results match vanilla when they should. */
	public static final boolean DEBUG_COMPARE_LIGHTING;
	public static final boolean FIX_SMOOTH_LIGHTING_OFFSET;
	public static final boolean FIX_MEAN_LIGHT_CALCULATION;
	/**
	 * Same value as {@link #FIX_MEAN_LIGHT_CALCULATION} because it is only required when the mean formula is changed,
	 * but different field to clearly separate code paths where we change emissive handling.
	 */
	public static final boolean FIX_EMISSIVE_LIGHTING;
	public static final boolean FIX_EXTERIOR_VERTEX_LIGHTING;
	public static final boolean FIX_LUMINOUS_AO_SHADE;

	public static final Logger LOGGER = LoggerFactory.getLogger(NotebookBuiltinRenderer.class);

	private static boolean asBoolean(String property, boolean defValue) {
		switch (asTriState(property)) {
		case TRUE:
			return true;
		case FALSE:
			return false;
		default:
			return defValue;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends Enum> T asEnum(String property, T defValue) {
		if (property == null || property.isEmpty()) {
			return defValue;
		} else {
			for (Enum obj : defValue.getClass().getEnumConstants()) {
				if (property.equalsIgnoreCase(obj.name())) {
					//noinspection unchecked
					return (T) obj;
				}
			}

			return defValue;
		}
	}

	private static TriState asTriState(String property) {
		if (property == null || property.isEmpty()) {
			return TriState.DEFAULT;
		} else {
			switch (property.toLowerCase(Locale.ROOT)) {
			case "true":
				return TriState.TRUE;
			case "false":
				return TriState.FALSE;
			case "auto":
			default:
				return TriState.DEFAULT;
			}
		}
	}

	static {
		File configDir = NotebookLoader.getInstance().getConfigDir().resolve("notebook").toFile();

		if (!configDir.exists()) {
			if (!configDir.mkdir()) {
				LOGGER.warn("[Indigo] Could not create configuration directory: " + configDir.getAbsolutePath());
			}
		}

		File configFile = new File(configDir, "notebook-renderer.properties");
		Properties properties = new Properties();

		if (configFile.exists()) {
			try (FileInputStream stream = new FileInputStream(configFile)) {
				properties.load(stream);
			} catch (IOException e) {
				LOGGER.warn("[Notebook Renderer] Could not read property file '" + configFile.getAbsolutePath() + "'", e);
			}
		}

		final boolean forceCompatibility = RendererMixinConfigPlugin.shouldForceCompatibility();
		ENSURE_VERTEX_FORMAT_COMPATIBILITY = forceCompatibility;
		// necessary because OF alters the BakedModel vertex format and will confuse the fallback model consumer
		ALWAYS_TESSELATE_INDIGO = !forceCompatibility && asBoolean((String) properties.computeIfAbsent("always-tesselate-blocks", (a) -> "auto"), true);
		AMBIENT_OCCLUSION_MODE = asEnum((String) properties.computeIfAbsent("ambient-occlusion-mode", (a) -> "hybrid"), AoConfig.HYBRID);
		DEBUG_COMPARE_LIGHTING = asBoolean((String) properties.computeIfAbsent("debug-compare-lighting", (a) -> "auto"), false);
		FIX_SMOOTH_LIGHTING_OFFSET = asBoolean((String) properties.computeIfAbsent("fix-smooth-lighting-offset", (a) -> "auto"), true);
		FIX_MEAN_LIGHT_CALCULATION = asBoolean((String) properties.computeIfAbsent("fix-mean-light-calculation", (a) -> "auto"), true);
		FIX_EMISSIVE_LIGHTING = FIX_MEAN_LIGHT_CALCULATION;
		FIX_EXTERIOR_VERTEX_LIGHTING = asBoolean((String) properties.computeIfAbsent("fix-exterior-vertex-lighting", (a) -> "auto"), true);
		FIX_LUMINOUS_AO_SHADE = asBoolean((String) properties.computeIfAbsent("fix-luminous-block-ambient-occlusion", (a) -> "auto"), false);

		try (FileOutputStream stream = new FileOutputStream(configFile)) {
			properties.store(stream, "Notebook Renderer properties file");
		} catch (IOException e) {
			LOGGER.warn("[Notebook Renderer] Could not store property file '" + configFile.getAbsolutePath() + "'", e);
		}
	}

	@Override
	public void onInitializeClient() {
		if (RendererMixinConfigPlugin.shouldApplyIndigo()) {
			LOGGER.info("[Notebook Renderer] Registering Notebook renderer!");

			if (RendererMixinConfigPlugin.shouldForceCompatibility()) {
				LOGGER.info("[Notebook Renderer] Compatibility mode enabled.");
			}

			RendererAccess.INSTANCE.registerRenderer(NotebookRenderer.INSTANCE);
		} else {
			LOGGER.info("[Indigo] Different rendering plugin detected; not applying Indigo.");
		}
	}
}

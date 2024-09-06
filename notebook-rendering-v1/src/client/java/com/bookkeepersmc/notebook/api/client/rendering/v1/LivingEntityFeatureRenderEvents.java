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
package com.bookkeepersmc.notebook.api.client.rendering.v1;

import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;

/**
 * Events related to living entity {@link FeatureRenderer}s.
 * To register a renderer, see {@link LivingEntityFeatureRendererRegistrationCallback} instead.
 */
public final class LivingEntityFeatureRenderEvents {
	/**
	 * An event that can prevent capes from rendering.
	 */
	public static final Event<AllowCapeRender> ALLOW_CAPE_RENDER = EventFactory.createArrayBacked(AllowCapeRender.class, listeners -> state -> {
		for (AllowCapeRender listener : listeners) {
			if (!listener.allowCapeRender(state)) {
				return false;
			}
		}

		return true;
	});

	@FunctionalInterface
	public interface AllowCapeRender {
		/**
		 * @return false to prevent rendering the cape
		 */
		boolean allowCapeRender(PlayerEntityRenderState state);
	}

	private LivingEntityFeatureRenderEvents() {
	}
}

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
package com.bookkeepersmc.notebook.api.renderer.v1.model;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;

/**
 * An interface to be implemented by models that wrap and replace another model, such as {@link ForwardingBakedModel}.
 * This allows mods to access the wrapped model without having to know the exact type of the wrapper model.
 *
 * <p>If you need to access data stored in one of your {@link BakedModel} subclasses,
 * and you would normally access the model by its identifier and then cast it:
 * call {@link #unwrap(BakedModel)} on the model before, in case another mod is wrapping your model to alter its rendering.
 */
public interface WrapperBakedModel {
	/**
	 * Return the wrapped model, if there is one at the moment, or {@code null} otherwise.
	 *
	 * <p>If there are multiple layers of wrapping, this method does not necessarily return the innermost model.
	 */
	@Nullable
	BakedModel getWrappedModel();

	/**
	 * Fully unwrap a model, i.e. return the innermost model.
	 */
	static BakedModel unwrap(BakedModel model) {
		while (model instanceof WrapperBakedModel wrapper) {
			BakedModel wrapped = wrapper.getWrappedModel();

			if (wrapped == null) {
				return model;
			} else if (wrapped == model) {
				throw new IllegalArgumentException("Model " + model + " is wrapping itself!");
			} else {
				model = wrapped;
			}
		}

		return model;
	}
}

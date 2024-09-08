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
package com.bookkeepersmc.notebook.api.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public enum TriState {
	/**
	 * Represents the boolean value of {@code false}.
	 */
	FALSE,
	/**
	 * Represents a value that refers to a "default" value, often as a fallback.
	 */
	DEFAULT,
	/**
	 * Represents the boolean value of {@code true}.
	 */
	TRUE;

	/**
	 * Gets the corresponding tri-state from a boolean value.
	 *
	 * @param bool the boolean value
	 * @return {@link TriState#TRUE} or {@link TriState#FALSE} depending on the value of the boolean.
	 */
	public static TriState of(boolean bool) {
		return bool ? TRUE : FALSE;
	}

	/**
	 * Gets a tri-state from a nullable boxed boolean.
	 *
	 * @param bool the boolean value
	 * @return {@link TriState#DEFAULT} if {@code null}.
	 * Otherwise {@link TriState#TRUE} or {@link TriState#FALSE} depending on the value of the boolean.
	 */
	public static TriState of(@Nullable Boolean bool) {
		return bool == null ? DEFAULT : of(bool.booleanValue());
	}

	/**
	 * Gets the value of the tri-state.
	 *
	 * @return true if the tri-state is {@link TriState#TRUE},
	 * otherwise false.
	 */
	public boolean get() {
		return this == TRUE;
	}

	/**
	 * Gets the value of the tri-state as a boxed, nullable boolean.
	 *
	 * @return {@code null} if {@link TriState#DEFAULT}.
	 * Otherwise {@code true} if {@link TriState#TRUE} or {@code false} if {@link TriState#FALSE}.
	 */
	@Nullable
	public Boolean getBoxed() {
		return this == DEFAULT ? null : this.get();
	}

	/**
	 * Gets the value of this tri-state.
	 * If the value is {@link TriState#DEFAULT} then use the supplied value.
	 *
	 * @param value the value to fall back to
	 * @return the value of the tri-state or the supplied value if {@link TriState#DEFAULT}.
	 */
	public boolean orElse(boolean value) {
		return this == DEFAULT ? value : this.get();
	}

	/**
	 * Gets the value of this tri-state.
	 * If the value is {@link TriState#DEFAULT} then use the supplied value.
	 *
	 * @param supplier the supplier used to get the value to fall back to
	 * @return the value of the tri-state or the value of the supplier if the tri-state is {@link TriState#DEFAULT}.
	 */
	public boolean orElseGet(BooleanSupplier supplier) {
		return this == DEFAULT ? supplier.getAsBoolean() : this.get();
	}

	/**
	 * Maps the boolean value of this tri-state if it is {@link TriState#TRUE} or {@link TriState#FALSE}.
	 *
	 * @param mapper the mapper to use
	 * @param <T> the type of object being supplier by the mapper
	 * @return an optional containing the mapped value; {@link Optional#empty()} if the tri-state is {@link TriState#DEFAULT} or the value provided by the mapper is {@code null}.
	 */
	public <T> Optional<T> map(BooleanFunction<@Nullable ? extends T> mapper) {
		Objects.requireNonNull(mapper, "Mapper function cannot be null");

		if (this == DEFAULT) {
			return Optional.empty();
		}

		return Optional.ofNullable(mapper.apply(this.get()));
	}

	/**
	 * Gets the value of this tri-state, or throws an exception if this tri-state's value is {@link TriState#DEFAULT}.
	 *
	 * @param exceptionSupplier the supplying function that produces an exception to be thrown
	 * @param <X> Type of the exception to be thrown
	 * @return the value
	 * @throws X if the value is {@link TriState#DEFAULT}
	 */
	public <X extends Throwable> boolean orElseThrow(Supplier<X> exceptionSupplier) throws X {
		if (this != DEFAULT) {
			return this.get();
		}

		throw exceptionSupplier.get();
	}
}

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
package com.bookkeepersmc.notebook.api.item.v1;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.component.DataComponentMap;
import net.minecraft.item.Item;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;
/**
 * Events to modify the default {@link DataComponentMap} of items.
 */
public final class DefaultItemComponentEvents {
	/**
	 * Event used to add or remove data components to known items.
	 */
	public static final Event<ModifyCallback> MODIFY = EventFactory.createArrayBacked(ModifyCallback.class, listeners -> context -> {
		for (ModifyCallback listener : listeners) {
			listener.modify(context);
		}
	});

	private DefaultItemComponentEvents() {
	}

	public interface ModifyContext {
		/**
		 * Modify the default data components of the specified item.
		 *
		 * @param itemPredicate A predicate to match items to modify
		 * @param builderConsumer A consumer that provides a {@link DataComponentMap.Builder} to modify the item's components.
		 */
		void modify(Predicate<Item> itemPredicate, BiConsumer<DataComponentMap.Builder, Item> builderConsumer);

		/**
		 * Modify the default data components of the specified item.
		 *
		 * @param item The item to modify
		 * @param builderConsumer A consumer that provides a {@link DataComponentMap.Builder} to modify the item's components.
		 */
		default void modify(Item item, Consumer<DataComponentMap.Builder> builderConsumer) {
			modify(Predicate.isEqual(item), (builder, _item) -> builderConsumer.accept(builder));
		}

		/**
		 * Modify the default data components of the specified items.
		 * @param items The items to modify
		 * @param builderConsumer A consumer that provides a {@link DataComponentMap.Builder} to modify the item's components.
		 */
		default void modify(Collection<Item> items, BiConsumer<DataComponentMap.Builder, Item> builderConsumer) {
			modify(items::contains, builderConsumer);
		}
	}

	@FunctionalInterface
	public interface ModifyCallback {
		/**
		 * Modify the default data components of items using the provided {@link ModifyContext} instance.
		 *
		 * @param context The context to modify items
		 */
		void modify(ModifyContext context);
	}
}

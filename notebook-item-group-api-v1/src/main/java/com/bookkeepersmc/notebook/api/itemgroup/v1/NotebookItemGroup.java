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
package com.bookkeepersmc.notebook.api.itemgroup.v1;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import com.bookkeepersmc.notebook.impl.itemgroup.NotebookItemGroupBuilderImpl;

/**
 * Contains a method to create an item group builder.
 */
public final class NotebookItemGroup {
	private NotebookItemGroup() {
	}

	/**
	 * Creates a new builder for {@link ItemGroup}. Item groups are used to group items in the creative
	 * inventory.
	 *
	 * <p>You must register the newly created {@link ItemGroup} to the {@link Registries#ITEM_GROUP} registry.
	 *
	 * <p>You must also set a display name by calling {@link ItemGroup.Builder#name(Text)}
	 *
	 * <p>Example:
	 *
	 * <pre>{@code
	 * private static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MOD_ID, "test_group"));
	 *
	 * @Override
	 * public void onInitialize() {
	 *    Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
	 *       .displayName(Text.translatable("modid.test_group"))
	 *       .icon(() -> new ItemStack(Items.DIAMOND))
	 *       .entries((context, entries) -> {
	 *          entries.add(TEST_ITEM);
	 *       })
	 *       .build()
	 *    );
	 * }
	 * }</pre>
	 *
	 * @return a new {@link ItemGroup.Builder} instance
	 */
	public static ItemGroup.Builder builder() {
		return new NotebookItemGroupBuilderImpl();
	}
}

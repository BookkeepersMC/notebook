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

import java.util.HashMap;
import java.util.function.BiConsumer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public final class BlockEntityRendererRegistryImpl {
	private static final HashMap<BlockEntityType<?>, BlockEntityRendererFactory<?>> MAP = new HashMap<>();
	private static BiConsumer<BlockEntityType<?>, BlockEntityRendererFactory<?>> handler = (type, function) -> MAP.put(type, function);

	public static <E extends BlockEntity> void register(BlockEntityType<E> blockEntityType, BlockEntityRendererFactory<? super E> blockEntityRendererFactory) {
		handler.accept(blockEntityType, blockEntityRendererFactory);
	}

	public static void setup(BiConsumer<BlockEntityType<?>, BlockEntityRendererFactory<?>> vanillaHandler) {
		MAP.forEach(vanillaHandler);
		handler = vanillaHandler;
	}

	private BlockEntityRendererRegistryImpl() {
	}
}

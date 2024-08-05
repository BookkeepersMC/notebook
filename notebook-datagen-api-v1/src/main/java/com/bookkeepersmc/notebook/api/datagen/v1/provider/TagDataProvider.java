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
package com.bookkeepersmc.notebook.api.datagen.v1.provider;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;
import com.bookkeepersmc.notebook.impl.datagen.ForcedTagEntry;

public abstract class TagDataProvider<T> extends TagsProvider<T> {
	public TagDataProvider(NotebookDataOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registryKey, registriesFuture);
	}

	protected abstract void addTags(HolderLookup.Provider wrapperLookup);

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected ResourceKey<T> reverseLookup(T element) {
		Registry registry = BuiltInRegistries.REGISTRY.get((ResourceKey) registryKey);

		if (registry != null) {
			Optional<Holder<T>> key = registry.getResourceKey(element);

			if (key.isPresent()) {
				return (ResourceKey<T>) key.get();
			}
		}

		throw new UnsupportedOperationException("Adding objects is not supported by " + getClass());
	}

	@Override
	protected NotebookTagBuilder tag(TagKey<T> tag) {
		return new NotebookTagBuilder(super.tag(tag));
	}

	public abstract static class BlockTagProvider extends TagDataProvider<Block> {
		public BlockTagProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.BLOCK, registriesFuture);
		}

		@Override
		protected ResourceKey<Block> reverseLookup(Block element) {
			return element.builtInRegistryHolder().key();
		}
	}

	public abstract static class BlockEntityTypeTagProvider extends TagDataProvider<BlockEntityType<?>> {
		public BlockEntityTypeTagProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.BLOCK_ENTITY_TYPE, completableFuture);
		}

		@Override
		protected ResourceKey<BlockEntityType<?>> reverseLookup(BlockEntityType<?> element) {
			return element.builtInRegistryHolder().key();
		}
	}

	public abstract static class ItemTagProvider extends TagDataProvider<Item> {
		@Nullable
		private final Function<TagKey<Block>, TagBuilder> blockTagBuilderProvider;

		public ItemTagProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable TagDataProvider.BlockTagProvider blockTagProvider) {
			super(output, Registries.ITEM, completableFuture);

			this.blockTagBuilderProvider = blockTagProvider == null ? null : blockTagProvider::getOrCreateRawBuilder;
		}

		public ItemTagProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			this(output, completableFuture, null);
		}

		public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
			TagBuilder blockTagBuilder = Objects.requireNonNull(this.blockTagBuilderProvider, "Pass Block tag provider via constructor to use copy").apply(blockTag);
			TagBuilder itemTagBuilder = this.getOrCreateRawBuilder(itemTag);
			blockTagBuilder.build().forEach(itemTagBuilder::add);
		}

		@Override
		protected ResourceKey<Item> reverseLookup(Item element) {
			return element.builtInRegistryHolder().key();
		}
	}

	public abstract static class FluidTagProvider extends TagDataProvider<Fluid> {
		public FluidTagProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.FLUID, completableFuture);
		}

		@Override
		protected ResourceKey<Fluid> reverseLookup(Fluid element) {
			return element.builtInRegistryHolder().key();
		}
	}

	public abstract static class EnchantmentTagProvider extends TagDataProvider<Enchantment> {
		public EnchantmentTagProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.ENCHANTMENT, completableFuture);
		}
	}

	public abstract static class EntityTypeTagProvider extends TagDataProvider<EntityType<?>> {
		public EntityTypeTagProvider(NotebookDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
			super(output, Registries.ENTITY_TYPE, completableFuture);
		}

		@Override
		protected ResourceKey<EntityType<?>> reverseLookup(EntityType<?> element) {
			return element.builtInRegistryHolder().key();
		}
	}

	public final class NotebookTagBuilder extends TagAppender<T> {
		private final TagAppender<T> parent;

		private NotebookTagBuilder(TagAppender<T> parent) {
			super(parent.builder);
			this.parent = parent;
		}

		public NotebookTagBuilder setReplace(boolean replace) {
			((com.bookkeepersmc.notebook.impl.datagen.NotebookTagBuilder) builder).notebook_setReplace(replace);
			return this;
		}

		public NotebookTagBuilder add(T element) {
			add(reverseLookup(element));
			return this;
		}

		@SafeVarargs
		public final NotebookTagBuilder add(T... element) {
			Stream.of(element).map(TagDataProvider.this::reverseLookup).forEach(this::add);
			return this;
		}

		@Override
		public NotebookTagBuilder add(ResourceKey<T> registryKey) {
			parent.add(registryKey);
			return this;
		}

		public NotebookTagBuilder add(ResourceLocation id) {
			builder.addElement(id);
			return this;
		}

		@Override
		public NotebookTagBuilder addOptional(ResourceLocation id) {
			parent.addOptional(id);
			return this;
		}

		public NotebookTagBuilder addOptional(ResourceKey<? extends T> registryKey) {
			return addOptional(registryKey.location());
		}

		@Override
		public NotebookTagBuilder addTag(TagKey<T> tag) {
			builder.addTag(tag.location());
			return this;
		}

		@Override
		public NotebookTagBuilder addOptionalTag(ResourceLocation id) {
			parent.addOptionalTag(id);
			return this;
		}

		public NotebookTagBuilder addOptionalTag(TagKey<T> tag) {
			return addOptionalTag(tag.location());
		}

		public NotebookTagBuilder forceAddTag(TagKey<T> tag) {
			builder.add(new ForcedTagEntry(TagEntry.element(tag.location())));
			return this;
		}

		public NotebookTagBuilder add(ResourceLocation... ids) {
			for (ResourceLocation id : ids) {
				add(id);
			}

			return this;
		}

		@SafeVarargs
		@Override
		public final NotebookTagBuilder add(ResourceKey<T>... registryKeys) {
			for (ResourceKey<T> registryKey : registryKeys) {
				add(registryKey);
			}

			return this;
		}
	}
}

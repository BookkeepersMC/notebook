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

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.data.DataPackOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.ResourceKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.StatType;
import net.minecraft.text.component.TextComponent;
import net.minecraft.text.component.TranslatableComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataOutput;

public abstract class LanguageDataProvider implements DataProvider {
	protected final NotebookDataOutput dataOutput;
	private final String languageCode;
	private final CompletableFuture<HolderLookup.Provider> registryLookup;

	protected LanguageDataProvider(NotebookDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		this(dataOutput, "en_us", registryLookup);
	}

	protected LanguageDataProvider(NotebookDataOutput dataOutput, String languageCode, CompletableFuture<HolderLookup.Provider> registryLookup) {
		this.dataOutput = dataOutput;
		this.languageCode = languageCode;
		this.registryLookup = registryLookup;
	}

	/**
	 * Implement this method to register languages.
	 *
	 * <p>Call {@link TranslationBuilder#add(String, String)} to add a translation.
	 */
	public abstract void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder);

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		TreeMap<String, String> translationEntries = new TreeMap<>();

		return this.registryLookup.thenCompose(lookup -> {
			generateTranslations(lookup, (String key, String value) -> {
				Objects.requireNonNull(key);
				Objects.requireNonNull(value);

				if (translationEntries.containsKey(key)) {
					throw new RuntimeException("Existing translation key found - " + key + " - Duplicate will be ignored.");
				}

				translationEntries.put(key, value);
			});

			JsonObject langEntryJson = new JsonObject();

			for (Map.Entry<String, String> entry : translationEntries.entrySet()) {
				langEntryJson.addProperty(entry.getKey(), entry.getValue());
			}

			return DataProvider.writeToPath(writer, langEntryJson, getLangFilePath(this.languageCode));
		});
	}

	private Path getLangFilePath(String code) {
		return dataOutput
				.createPathResolver(DataPackOutput.Type.RESOURCE_PACK, "lang")
				.resolveJsonFile(Identifier.of(dataOutput.getModId(), code));
	}

	@Override
	public String getDescription() {
		return "Language (%s)".formatted(languageCode);
	}

	@ApiStatus.NonExtendable
	@FunctionalInterface
	public interface TranslationBuilder {
		void add(String translationKey, String value);

		default void add(Item item, String value) {
			add(item.method_7876(), value);
		}

		default void add(Block block, String value) {
			add(block.getTranslationKey(), value);
		}

		default void add(ResourceKey<ItemGroup> registryKey, String value) {
			final ItemGroup group = BuiltInRegistries.ITEM_GROUP.getOrThrow(registryKey);
			final TextComponent content = group.getName().asComponent();

			if (content instanceof TranslatableComponent translatableTextContent) {
				add(translatableTextContent.getKey(), value);
				return;
			}

			throw new UnsupportedOperationException("Cannot add language entry for ItemGroup (%s) as the display name is not translatable.".formatted(group.getName().getString()));
		}

		default void add(EntityType<?> entityType, String value) {
			add(entityType.getTranslationKey(), value);
		}

		default void addEnchantment(ResourceKey<Enchantment> enchantment, String value) {
			add(Util.createTranslationKey("enchantment", enchantment.getValue()), value);
		}

		default void add(Holder<EntityAttribute> entityAttribute, String value) {
			add(entityAttribute.getValue().getTranslationKey(), value);
		}

		default void add(StatType<?> statType, String value) {
			add("stat_type." + BuiltInRegistries.STAT_TYPE.getKey(statType).toString().replace(':', '.'), value);
		}

		default void add(StatusEffect statusEffect, String value) {
			add(statusEffect.getTranslationKey(), value);
		}

		default void add(Identifier identifier, String value) {
			add(identifier.toTranslationKey(), value);
		}

		default void add(TagKey<?> tagKey, String value) {
			add(tagKey.getTranslationKey(), value);
		}

		default void add(Path existingLanguageFile) throws IOException {
			try (Reader reader = Files.newBufferedReader(existingLanguageFile)) {
				JsonObject translations = JsonParser.parseReader(reader).getAsJsonObject();

				for (String key : translations.keySet()) {
					add(key, translations.get(key).getAsString());
				}
			}
		}
	}
}

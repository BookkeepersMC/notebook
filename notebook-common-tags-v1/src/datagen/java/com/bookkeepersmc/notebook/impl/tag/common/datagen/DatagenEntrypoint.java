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
package com.bookkeepersmc.notebook.impl.tag.common.datagen;

import com.bookkeepersmc.notebook.api.datagen.v1.DataGeneratorEntrypoint;
import com.bookkeepersmc.notebook.api.datagen.v1.NotebookDataGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.BiomeTagGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.BlockTagGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.EnchantmentTagGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.EnglishTagLangGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.EntityTypeTagGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.FluidTagGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.ItemTagGenerator;
import com.bookkeepersmc.notebook.impl.tag.common.datagen.generators.StructureTagGenerator;

public class DatagenEntrypoint implements DataGeneratorEntrypoint {
	@Override
	public void onDataGeneratorInit(NotebookDataGenerator notebookDataGenerator) {
		final NotebookDataGenerator.Pack pack = notebookDataGenerator.createPack();

		BlockTagGenerator blockTags = pack.addProvider(BlockTagGenerator::new);
		pack.addProvider((output, provider) -> new ItemTagGenerator(output, provider, blockTags));
		pack.addProvider(FluidTagGenerator::new);
		pack.addProvider(EnchantmentTagGenerator::new);
		pack.addProvider(BiomeTagGenerator::new);
		pack.addProvider(StructureTagGenerator::new);
		pack.addProvider(EntityTypeTagGenerator::new);
		pack.addProvider(EnglishTagLangGenerator::new);
	}

	@Override
	public String getEffectiveModId() {
		return "notebook-common-tags-v1";
	}
}

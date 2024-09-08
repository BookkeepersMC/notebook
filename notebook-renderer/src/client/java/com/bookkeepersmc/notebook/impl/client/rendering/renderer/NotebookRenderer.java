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
package com.bookkeepersmc.notebook.impl.client.rendering.renderer;

import java.util.HashMap;

import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.renderer.v1.Renderer;
import com.bookkeepersmc.notebook.api.renderer.v1.material.MaterialFinder;
import com.bookkeepersmc.notebook.api.renderer.v1.material.RenderMaterial;
import com.bookkeepersmc.notebook.api.renderer.v1.mesh.MeshBuilder;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.material.MaterialFinderImpl;
import com.bookkeepersmc.notebook.impl.client.rendering.renderer.mesh.MeshBuilderImpl;

/**
 * The Fabric default renderer implementation. Supports all
 * features defined in the API except shaders and offers no special materials.
 */
public class NotebookRenderer implements Renderer {
	public static final NotebookRenderer INSTANCE = new NotebookRenderer();

	public static final RenderMaterial MATERIAL_STANDARD = INSTANCE.materialFinder().find();

	static {
		INSTANCE.registerMaterial(RenderMaterial.MATERIAL_STANDARD, MATERIAL_STANDARD);
	}

	private final HashMap<Identifier, RenderMaterial> materialMap = new HashMap<>();

	private NotebookRenderer() { }

	@Override
	public MeshBuilder meshBuilder() {
		return new MeshBuilderImpl();
	}

	@Override
	public MaterialFinder materialFinder() {
		return new MaterialFinderImpl();
	}

	@Override
	public RenderMaterial materialById(Identifier id) {
		return materialMap.get(id);
	}

	@Override
	public boolean registerMaterial(Identifier id, RenderMaterial material) {
		if (materialMap.containsKey(id)) return false;

		// cast to prevent acceptance of impostor implementations
		materialMap.put(id, material);
		return true;
	}
}

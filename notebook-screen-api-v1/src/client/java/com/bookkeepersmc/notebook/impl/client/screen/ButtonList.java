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
package com.bookkeepersmc.notebook.impl.client.screen;

import java.util.AbstractList;
import java.util.List;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public final class ButtonList extends AbstractList<AbstractWidget> {
	private final List<Renderable> drawables;
	private final List<NarratableEntry> selectables;
	private final List<GuiEventListener> children;

	public ButtonList(List<Renderable> drawables, List<NarratableEntry> selectables, List<GuiEventListener> children) {
		this.drawables = drawables;
		this.selectables = selectables;
		this.children = children;
	}

	@Override
	public AbstractWidget get(int index) {
		final int drawableIndex = translateIndex(drawables, index, false);
		return (AbstractWidget) drawables.get(drawableIndex);
	}

	@Override
	public AbstractWidget set(int index, AbstractWidget element) {
		final int drawableIndex = translateIndex(drawables, index, false);
		drawables.set(drawableIndex, element);

		final int selectableIndex = translateIndex(selectables, index, false);
		selectables.set(selectableIndex, element);

		final int childIndex = translateIndex(children, index, false);
		return (AbstractWidget) children.set(childIndex, element);
	}

	@Override
	public void add(int index, AbstractWidget element) {
		// ensure no duplicates
		final int duplicateIndex = drawables.indexOf(element);

		if (duplicateIndex >= 0) {
			drawables.remove(element);
			selectables.remove(element);
			children.remove(element);

			if (duplicateIndex <= translateIndex(drawables, index, true)) {
				index--;
			}
		}

		final int drawableIndex = translateIndex(drawables, index, true);
		drawables.add(drawableIndex, element);

		final int selectableIndex = translateIndex(selectables, index, true);
		selectables.add(selectableIndex, element);

		final int childIndex = translateIndex(children, index, true);
		children.add(childIndex, element);
	}

	@Override
	public AbstractWidget remove(int index) {
		index = translateIndex(drawables, index, false);

		final AbstractWidget removedButton = (AbstractWidget) drawables.remove(index);
		this.selectables.remove(removedButton);
		this.children.remove(removedButton);

		return removedButton;
	}

	@Override
	public int size() {
		int ret = 0;

		for (Renderable renderable : drawables) {
			if (renderable instanceof AbstractWidget) {
				ret++;
			}
		}

		return ret;
	}

	private int translateIndex(List<?> list, int index, boolean allowAfter) {
		int remaining = index;

		for (int i = 0, max = list.size(); i < max; i++) {
			if (list.get(i) instanceof AbstractWidget) {
				if (remaining == 0) {
					return i;
				}

				remaining--;
			}
		}

		if (allowAfter && remaining == 0) {
			return list.size();
		}

		throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, index - remaining));
	}
}

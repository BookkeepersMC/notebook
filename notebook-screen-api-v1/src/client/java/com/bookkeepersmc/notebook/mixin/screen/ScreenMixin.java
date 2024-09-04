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
package com.bookkeepersmc.notebook.mixin.screen;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenEvents;
import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenKeyboardEvents;
import com.bookkeepersmc.notebook.api.client.screen.v1.ScreenMouseEvents;
import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.impl.client.screen.ButtonList;
import com.bookkeepersmc.notebook.impl.client.screen.ScreenEventFactory;
import com.bookkeepersmc.notebook.impl.client.screen.ScreenExtensions;

@Mixin(Screen.class)
abstract class ScreenMixin implements ScreenExtensions {
	@Shadow
	@Final
	protected List<Selectable> selectables;
	@Shadow
	@Final
	protected List<Element> children;
	@Shadow
	@Final
	protected List<Drawable> drawables;

	@Unique
	private ButtonList notebookButtons;
	@Unique
	private Event<ScreenEvents.Remove> removeEvent;
	@Unique
	private Event<ScreenEvents.BeforeTick> beforeTickEvent;
	@Unique
	private Event<ScreenEvents.AfterTick> afterTickEvent;
	@Unique
	private Event<ScreenEvents.BeforeRender> beforeRenderEvent;
	@Unique
	private Event<ScreenEvents.AfterRender> afterRenderEvent;

	// Keyboard
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyPress> allowKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyPress> beforeKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyPress> afterKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyRelease> allowKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyRelease> beforeKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyRelease> afterKeyReleaseEvent;

	// Mouse
	@Unique
	private Event<ScreenMouseEvents.AllowMouseClick> allowMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseClick> beforeMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseClick> afterMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseRelease> allowMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseRelease> beforeMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseRelease> afterMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseScroll> allowMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseScroll> beforeMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseScroll> afterMouseScrollEvent;

	@Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
	private void beforeInitScreen(Minecraft client, int width, int height, CallbackInfo ci) {
		beforeInit(client, width, height);
	}

	@Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
	private void afterInitScreen(Minecraft client, int width, int height, CallbackInfo ci) {
		afterInit(client, width, height);
	}

	@Inject(method = "resize", at = @At("HEAD"))
	private void beforeResizeScreen(Minecraft client, int width, int height, CallbackInfo ci) {
		beforeInit(client, width, height);
	}

	@Inject(method = "resize", at = @At("TAIL"))
	private void afterResizeScreen(Minecraft client, int width, int height, CallbackInfo ci) {
		afterInit(client, width, height);
	}

	@Unique
	private void beforeInit(Minecraft client, int width, int height) {
		// All elements are repopulated on the screen, so we need to reinitialize all events
		this.notebookButtons = null;
		this.removeEvent = ScreenEventFactory.createRemoveEvent();
		this.beforeRenderEvent = ScreenEventFactory.createBeforeRenderEvent();
		this.afterRenderEvent = ScreenEventFactory.createAfterRenderEvent();
		this.beforeTickEvent = ScreenEventFactory.createBeforeTickEvent();
		this.afterTickEvent = ScreenEventFactory.createAfterTickEvent();

		// Keyboard
		this.allowKeyPressEvent = ScreenEventFactory.createAllowKeyPressEvent();
		this.beforeKeyPressEvent = ScreenEventFactory.createBeforeKeyPressEvent();
		this.afterKeyPressEvent = ScreenEventFactory.createAfterKeyPressEvent();
		this.allowKeyReleaseEvent = ScreenEventFactory.createAllowKeyReleaseEvent();
		this.beforeKeyReleaseEvent = ScreenEventFactory.createBeforeKeyReleaseEvent();
		this.afterKeyReleaseEvent = ScreenEventFactory.createAfterKeyReleaseEvent();

		// Mouse
		this.allowMouseClickEvent = ScreenEventFactory.createAllowMouseClickEvent();
		this.beforeMouseClickEvent = ScreenEventFactory.createBeforeMouseClickEvent();
		this.afterMouseClickEvent = ScreenEventFactory.createAfterMouseClickEvent();
		this.allowMouseReleaseEvent = ScreenEventFactory.createAllowMouseReleaseEvent();
		this.beforeMouseReleaseEvent = ScreenEventFactory.createBeforeMouseReleaseEvent();
		this.afterMouseReleaseEvent = ScreenEventFactory.createAfterMouseReleaseEvent();
		this.allowMouseScrollEvent = ScreenEventFactory.createAllowMouseScrollEvent();
		this.beforeMouseScrollEvent = ScreenEventFactory.createBeforeMouseScrollEvent();
		this.afterMouseScrollEvent = ScreenEventFactory.createAfterMouseScrollEvent();

		ScreenEvents.BEFORE_INIT.invoker().beforeInit(client, (Screen) (Object) this, width, height);
	}

	@Unique
	private void afterInit(Minecraft client, int width, int height) {
		ScreenEvents.AFTER_INIT.invoker().afterInit(client, (Screen) (Object) this, width, height);
	}

	@Override
	public List<ClickableWidget> notebook_getButtons() {
		// Lazy init to make the list access safe after Screen#init
		if (this.notebookButtons == null) {
			this.notebookButtons = new ButtonList(this.drawables, this.selectables, this.children);
		}

		return this.notebookButtons;
	}

	@Unique
	private <T> Event<T> ensureEventsAreInitialized(Event<T> event) {
		if (event == null) {
			throw new IllegalStateException(String.format("[notebook-screen-api-v1] The current screen (%s) has not been correctly initialised, please send this crash log to the mod author. This is usually caused by calling setScreen on the wrong thread.", this.getClass().getName()));
		}

		return event;
	}

	@Override
	public Event<ScreenEvents.Remove> notebook_getRemoveEvent() {
		return ensureEventsAreInitialized(this.removeEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeTick> notebook_getBeforeTickEvent() {
		return ensureEventsAreInitialized(this.beforeTickEvent);
	}

	@Override
	public Event<ScreenEvents.AfterTick> notebook_getAfterTickEvent() {
		return ensureEventsAreInitialized(this.afterTickEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeRender> notebook_getBeforeRenderEvent() {
		return ensureEventsAreInitialized(this.beforeRenderEvent);
	}

	@Override
	public Event<ScreenEvents.AfterRender> notebook_getAfterRenderEvent() {
		return ensureEventsAreInitialized(this.afterRenderEvent);
	}

	// Keyboard

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyPress> notebook_getAllowKeyPressEvent() {
		return ensureEventsAreInitialized(this.allowKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyPress> notebook_getBeforeKeyPressEvent() {
		return ensureEventsAreInitialized(this.beforeKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyPress> notebook_getAfterKeyPressEvent() {
		return ensureEventsAreInitialized(this.afterKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyRelease> notebook_getAllowKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.allowKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyRelease> notebook_getBeforeKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.beforeKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyRelease> notebook_getAfterKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.afterKeyReleaseEvent);
	}

	// Mouse

	@Override
	public Event<ScreenMouseEvents.AllowMouseClick> notebook_getAllowMouseClickEvent() {
		return ensureEventsAreInitialized(this.allowMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseClick> notebook_getBeforeMouseClickEvent() {
		return ensureEventsAreInitialized(this.beforeMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseClick> notebook_getAfterMouseClickEvent() {
		return ensureEventsAreInitialized(this.afterMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseRelease> notebook_getAllowMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.allowMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseRelease> notebook_getBeforeMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.beforeMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseRelease> notebook_getAfterMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.afterMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseScroll> notebook_getAllowMouseScrollEvent() {
		return ensureEventsAreInitialized(this.allowMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseScroll> notebook_getBeforeMouseScrollEvent() {
		return ensureEventsAreInitialized(this.beforeMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseScroll> notebook_getAfterMouseScrollEvent() {
		return ensureEventsAreInitialized(this.afterMouseScrollEvent);
	}
}

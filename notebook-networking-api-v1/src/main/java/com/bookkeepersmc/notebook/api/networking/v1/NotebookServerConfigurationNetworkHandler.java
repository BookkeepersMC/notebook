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
package com.bookkeepersmc.notebook.api.networking.v1;

import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.util.Identifier;

/**
 * Extensions for {@link ServerConfigurationNetworkHandler}.
 * This interface is automatically implemented via Mixin and interface injection.
 */
public interface NotebookServerConfigurationNetworkHandler {
	/**
	 * Enqueues a {@link ConfigurationTask} task to be processed.
	 *
	 * <p>Before adding a task use {@link ServerConfigurationNetworking#canSend(ServerConfigurationNetworkHandler, Identifier)}
	 * to ensure that the client can process this task.
	 *
	 * <p>Once the client has handled the task a packet should be sent to the server.
	 * Upon receiving this packet the server should call {@link NotebookServerConfigurationNetworkHandler#completeTask(ConfigurationTask.Type)},
	 * otherwise the client cannot join the world.
	 *
	 * @param task the task
	 */
	default void addTask(ConfigurationTask task) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Completes the task identified by {@code key}.
	 *
	 * @param key the task key
	 * @throws IllegalStateException if the current task is not {@code key}
	 */
	default void completeTask(ConfigurationTask.Type key) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}

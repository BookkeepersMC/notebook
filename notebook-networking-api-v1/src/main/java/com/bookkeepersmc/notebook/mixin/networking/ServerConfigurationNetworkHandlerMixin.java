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
package com.bookkeepersmc.notebook.mixin.networking;

import java.util.Queue;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.ConnectedClientData;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.listener.AbstractServerPacketHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;

import com.bookkeepersmc.notebook.api.networking.v1.NotebookServerConfigurationNetworkHandler;
import com.bookkeepersmc.notebook.impl.networking.NetworkHandlerExtensions;
import com.bookkeepersmc.notebook.impl.networking.server.ServerConfigurationNetworkAddon;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ServerConfigurationNetworkHandler.class, priority = 900)
public abstract class ServerConfigurationNetworkHandlerMixin extends AbstractServerPacketHandler implements NetworkHandlerExtensions, NotebookServerConfigurationNetworkHandler {
	@Shadow
	@Nullable
	private ConfigurationTask currentTask;

	@Shadow
	protected abstract void finishCurrentTask(ConfigurationTask.Type key);

	@Shadow
	@Final
	private Queue<ConfigurationTask> tasks;

	@Shadow
	public abstract boolean isConnected();

	@Shadow
	public abstract void startConfiguration();

	@Unique
	private ServerConfigurationNetworkAddon addon;

	@Unique
	private boolean sentConfiguration;

	@Unique
	private boolean earlyTaskExecution;

	public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData arg) {
		super(server, connection, arg);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ServerConfigurationNetworkAddon((ServerConfigurationNetworkHandler) (Object) this, this.server);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		this.addon.lateInit();
	}

	@Inject(method = "startConfiguration", at = @At("HEAD"), cancellable = true)
	private void onClientReady(CallbackInfo ci) {
		// Send the initial channel registration packet
		if (this.addon.startConfiguration()) {
			assert currentTask == null;
			ci.cancel();
			return;
		}

		// Ready to start sending packets
		if (!sentConfiguration) {
			this.addon.preConfiguration();
			sentConfiguration = true;
			earlyTaskExecution = true;
		}

		// Run the early tasks
		if (earlyTaskExecution) {
			if (pollEarlyTasks()) {
				ci.cancel();
				return;
			} else {
				earlyTaskExecution = false;
			}
		}

		// All early tasks should have been completed
		assert currentTask == null;
		assert tasks.isEmpty();

		// Run the vanilla tasks.
		this.addon.configuration();
	}

	@Unique
	private boolean pollEarlyTasks() {
		if (!earlyTaskExecution) {
			throw new IllegalStateException("Early task execution has finished");
		}

		if (this.currentTask != null) {
			throw new IllegalStateException("Task " + this.currentTask.getType().id() + " has not finished yet");
		}

		if (!this.isConnected()) {
			return false;
		}

		final ConfigurationTask task = this.tasks.poll();

		if (task != null) {
			this.currentTask = task;
			task.start(this::send);
			return true;
		}

		return false;
	}

	@Override
	public ServerConfigurationNetworkAddon getAddon() {
		return addon;
	}

	@Override
	public void addTask(ConfigurationTask task) {
		tasks.add(task);
	}

	@Override
	public void completeTask(ConfigurationTask.Type key) {
		if (!earlyTaskExecution) {
			finishCurrentTask(key);
			return;
		}

		final ConfigurationTask.Type currentKey = this.currentTask != null ? this.currentTask.getType() : null;

		if (!key.equals(currentKey)) {
			throw new IllegalStateException("Unexpected request for task finish, current task: " + currentKey + ", requested: " + key);
		}

		this.currentTask = null;
		startConfiguration();
	}
}

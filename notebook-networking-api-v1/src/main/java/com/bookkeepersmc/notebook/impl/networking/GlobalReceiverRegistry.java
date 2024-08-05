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
package com.bookkeepersmc.notebook.impl.networking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;

public final class GlobalReceiverRegistry<H> {
	public static final int DEFAULT_CHANNEL_NAME_MAX_LENGTH = 128;
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalReceiverRegistry.class);

	private final PacketFlow side;
	private final ConnectionProtocol phase;
	@Nullable
	private final PayloadTypeRegistryImpl<?> payloadTypeRegistry;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<ResourceLocation, H> handlers = new HashMap<>();
	private final Set<AbstractNetworkAddon<H>> trackedAddons = new HashSet<>();

	public GlobalReceiverRegistry(PacketFlow side, ConnectionProtocol phase, @Nullable PayloadTypeRegistryImpl<?> payloadTypeRegistry) {
		this.side = side;
		this.phase = phase;
		this.payloadTypeRegistry = payloadTypeRegistry;

		if (payloadTypeRegistry != null) {
			assert phase == payloadTypeRegistry.getPhase();
			assert side == payloadTypeRegistry.getSide();
		}
	}

	@Nullable
	public H getHandler(ResourceLocation channelName) {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return this.handlers.get(channelName);
		} finally {
			lock.unlock();
		}
	}

	public boolean registerGlobalReceiver(ResourceLocation channelName, H handler) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(handler, "Channel handler cannot be null");

		if (NetworkingImpl.isReservedCommonChannel(channelName)) {
			throw new IllegalArgumentException(String.format("Cannot register handler for reserved channel with name \"%s\"", channelName));
		}

		assertPayloadType(channelName);

		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			final boolean replaced = this.handlers.putIfAbsent(channelName, handler) == null;

			if (replaced) {
				this.handleRegistration(channelName, handler);
			}

			return replaced;
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public H unregisterGlobalReceiver(ResourceLocation channelName) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		if (NetworkingImpl.isReservedCommonChannel(channelName)) {
			throw new IllegalArgumentException(String.format("Cannot unregister packet handler for reserved channel with name \"%s\"", channelName));
		}

		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			final H removed = this.handlers.remove(channelName);

			if (removed != null) {
				this.handleUnregistration(channelName);
			}

			return removed;
		} finally {
			lock.unlock();
		}
	}

	public Map<ResourceLocation, H> getHandlers() {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			return new HashMap<>(this.handlers);
		} finally {
			lock.unlock();
		}
	}

	public Set<ResourceLocation> getChannels() {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return new HashSet<>(this.handlers.keySet());
		} finally {
			lock.unlock();
		}
	}

	// State tracking methods

	public void startSession(AbstractNetworkAddon<H> addon) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			if (this.trackedAddons.add(addon)) {
				addon.registerChannels(handlers);
			}

			this.logTrackedAddonSize();
		} finally {
			lock.unlock();
		}
	}

	public void endSession(AbstractNetworkAddon<H> addon) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			this.logTrackedAddonSize();
			this.trackedAddons.remove(addon);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * In practice, trackedAddons should never contain more than the number of players.
	 */
	private void logTrackedAddonSize() {
		if (LOGGER.isTraceEnabled() && this.trackedAddons.size() > 1) {
			LOGGER.trace("{} receiver registry tracks {} addon instances", phase.id(), trackedAddons.size());
		}
	}

	private void handleRegistration(ResourceLocation channelName, H handler) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			this.logTrackedAddonSize();

			for (AbstractNetworkAddon<H> addon : this.trackedAddons) {
				addon.registerChannel(channelName, handler);
			}
		} finally {
			lock.unlock();
		}
	}

	private void handleUnregistration(ResourceLocation channelName) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			this.logTrackedAddonSize();

			for (AbstractNetworkAddon<H> addon : this.trackedAddons) {
				addon.unregisterChannel(channelName);
			}
		} finally {
			lock.unlock();
		}
	}

	public void assertPayloadType(ResourceLocation channelName) {
		if (payloadTypeRegistry == null) {
			return;
		}

		if (payloadTypeRegistry.get(channelName) == null) {
			throw new IllegalArgumentException(String.format("Cannot register handler as no payload type has been registered with name \"%s\" for %s %s", channelName, side, phase));
		}

		if (channelName.toString().length() > DEFAULT_CHANNEL_NAME_MAX_LENGTH) {
			throw new IllegalArgumentException(String.format("Cannot register handler for channel with name \"%s\" as it exceeds the maximum length of 128 characters", channelName));
		}
	}

	public ConnectionProtocol getPhase() {
		return phase;
	}
}

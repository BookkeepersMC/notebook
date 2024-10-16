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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;

/**
 * A network addon is a simple abstraction to hold information about a player's registered channels.
 *
 * @param <H> the channel handler type
 */
public abstract class AbstractNetworkAddon<H> {
	protected final GlobalReceiverRegistry<H> receiver;
	protected final Logger logger;
	// A lock is used due to possible access on netty's event loops and game thread at same times such as during dynamic registration
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	// Sync map should be fine as there is little read write competition
	// All access to this map is guarded by the lock
	private final Map<Identifier, H> handlers = new HashMap<>();
	private final AtomicBoolean disconnected = new AtomicBoolean(); // blocks redundant disconnect notifications

	protected AbstractNetworkAddon(GlobalReceiverRegistry<H> receiver, String description) {
		this.receiver = receiver;
		this.logger = LoggerFactory.getLogger(description);
	}

	public final void lateInit() {
		this.receiver.startSession(this);
		invokeInitEvent();
	}

	protected abstract void invokeInitEvent();

	public final void endSession() {
		this.receiver.endSession(this);
	}

	@Nullable
	public H getHandler(Identifier channel) {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return this.handlers.get(channel);
		} finally {
			lock.unlock();
		}
	}

	private void assertNotReserved(Identifier channel) {
		if (this.isReservedChannel(channel)) {
			throw new IllegalArgumentException(String.format("Cannot (un)register handler for reserved channel with name \"%s\"", channel));
		}
	}

	public void registerChannels(Map<Identifier, H> map) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			for (Map.Entry<Identifier, H> entry : map.entrySet()) {
				assertNotReserved(entry.getKey());

				boolean unique = this.handlers.putIfAbsent(entry.getKey(), entry.getValue()) == null;
				if (unique) handleRegistration(entry.getKey());
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean registerChannel(Identifier channelName, H handler) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(handler, "Packet handler cannot be null");
		assertNotReserved(channelName);

		receiver.assertPayloadType(channelName);

		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			final boolean replaced = this.handlers.putIfAbsent(channelName, handler) == null;

			if (replaced) {
				this.handleRegistration(channelName);
			}

			return replaced;
		} finally {
			lock.unlock();
		}
	}

	public H unregisterChannel(Identifier channelName) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		assertNotReserved(channelName);

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

	public Set<Identifier> getReceivableChannels() {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return new HashSet<>(this.handlers.keySet());
		} finally {
			lock.unlock();
		}
	}

	protected abstract void handleRegistration(Identifier channelName);

	protected abstract void handleUnregistration(Identifier channelName);

	public final void handleDisconnect() {
		if (disconnected.compareAndSet(false, true)) {
			invokeDisconnectEvent();
			endSession();
		}
	}

	protected abstract void invokeDisconnectEvent();

	/**
	 * Checks if a channel is considered a "reserved" channel.
	 * A reserved channel such as "minecraft:(un)register" has special handling and should not have any channel handlers registered for it.
	 *
	 * @param channelName the channel name
	 * @return whether the channel is reserved
	 */
	protected abstract boolean isReservedChannel(Identifier channelName);
}

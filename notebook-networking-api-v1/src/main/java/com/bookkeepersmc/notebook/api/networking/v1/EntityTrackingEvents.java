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

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;

/**
 * Events related to a tracking entities within a player's view distance.
 */
public final class EntityTrackingEvents {
	/**
	 * An event that is called before player starts tracking an entity.
	 * Typically, this occurs when an entity enters a client's view distance.
	 * This event is called before the player's client is sent the entity's {@link Entity#getAddEntityPacket(ServerEntity)}  spawn packet}.
	 */
	public static final Event<StartTracking> START_TRACKING = EventFactory.createArrayBacked(StartTracking.class, callbacks -> (trackedEntity, player) -> {
		for (StartTracking callback : callbacks) {
			callback.onStartTracking(trackedEntity, player);
		}
	});

	/**
	 * An event that is called after a player has stopped tracking an entity.
	 * The client at this point was sent a packet to {@link net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket destroy} the entity on the client.
	 * The entity still exists on the server.
	 */
	public static final Event<StopTracking> STOP_TRACKING = EventFactory.createArrayBacked(StopTracking.class, callbacks -> (trackedEntity, player) -> {
		for (StopTracking callback : callbacks) {
			callback.onStopTracking(trackedEntity, player);
		}
	});

	@FunctionalInterface
	public interface StartTracking {
		/**
		 * Called before an entity starts getting tracked by a player.
		 *
		 * @param trackedEntity the entity that will be tracked
		 * @param player the player that will track the entity
		 */
		void onStartTracking(Entity trackedEntity, ServerPlayer player);
	}

	@FunctionalInterface
	public interface StopTracking {
		/**
		 * Called after an entity stops getting tracked by a player.
		 *
		 * @param trackedEntity the entity that is no longer being tracked
		 * @param player the player that is no longer tracking the entity
		 */
		void onStopTracking(Entity trackedEntity, ServerPlayer player);
	}

	private EntityTrackingEvents() {
	}
}

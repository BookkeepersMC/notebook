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
package com.bookkeepersmc.notebook.api.event.lifecycle.v1;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import com.bookkeepersmc.notebook.api.event.Event;
import com.bookkeepersmc.notebook.api.event.EventFactory;

public final class ServerTickEvents {
	private ServerTickEvents() {
	}

	public static final Event<StartTick> START_SERVER_TICK = EventFactory.createArrayBacked(StartTick.class, callbacks -> server -> {
		for (StartTick event : callbacks) {
			event.onStartTick(server);
		}
	});

	public static final Event<EndTick> END_SERVER_TICK = EventFactory.createArrayBacked(EndTick.class, callbacks -> server -> {
		for (EndTick event : callbacks) {
			event.onEndTick(server);
		}
	});

	public static final Event<StartWorldTick> START_WORLD_TICK = EventFactory.createArrayBacked(StartWorldTick.class, callbacks -> world -> {
		for (StartWorldTick callback : callbacks) {
			callback.onStartTick(world);
		}
	});

	public static final Event<EndWorldTick> END_WORLD_TICK = EventFactory.createArrayBacked(EndWorldTick.class, callbacks -> world -> {
		for (EndWorldTick callback : callbacks) {
			callback.onEndTick(world);
		}
	});

	@FunctionalInterface
	public interface StartTick {
		void onStartTick(MinecraftServer server);
	}

	@FunctionalInterface
	public interface EndTick {
		void onEndTick(MinecraftServer server);
	}

	@FunctionalInterface
	public interface StartWorldTick {
		void onStartTick(ServerWorld world);
	}

	@FunctionalInterface
	public interface EndWorldTick {
		void onEndTick(ServerWorld world);
	}
}

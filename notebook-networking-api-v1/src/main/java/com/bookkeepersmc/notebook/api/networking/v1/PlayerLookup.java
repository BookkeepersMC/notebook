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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.ChunkManager;

import com.bookkeepersmc.notebook.mixin.networking.accessor.EntityTrackerAccessor;
import com.bookkeepersmc.notebook.mixin.networking.accessor.ServerChunkLoadingManagerAccessor;

/**
 * Helper methods to lookup players in a server.
 *
 * <p>The word "tracking" means that an entity/chunk on the server is known to a player's client (within in view distance) and the (block) entity should notify tracking clients of changes.
 *
 * <p>These methods should only be called on the server thread and only be used on logical a server.
 */
public final class PlayerLookup {
	/**
	 * Gets all the players on the minecraft server.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * @param server the server
	 * @return all players on the server
	 */
	public static Collection<ServerPlayerEntity> all(MinecraftServer server) {
		Objects.requireNonNull(server, "The server cannot be null");

		// return an immutable collection to guard against accidental removals.
		if (server.getPlayerManager() != null) {
			return Collections.unmodifiableCollection(server.getPlayerManager().getPlayerList());
		}

		return Collections.emptyList();
	}

	/**
	 * Gets all the players in a server world.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * @param world the server world
	 * @return the players in the server world
	 */
	public static Collection<ServerPlayerEntity> world(ServerWorld world) {
		Objects.requireNonNull(world, "The world cannot be null");

		// return an immutable collection to guard against accidental removals.
		return Collections.unmodifiableCollection(world.getPlayers());
	}

	/**
	 * Gets all players tracking a chunk in a server world.
	 *
	 * @param world the server world
	 * @param pos   the chunk in question
	 * @return the players tracking the chunk
	 */
	public static Collection<ServerPlayerEntity> tracking(ServerWorld world, ChunkPos pos) {
		Objects.requireNonNull(world, "The world cannot be null");
		Objects.requireNonNull(pos, "The chunk pos cannot be null");

		return world.getChunkManager().delegate.getPlayersWatchingChunk(pos, false);
	}

	/**
	 * Gets all players tracking an entity in a server world.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * <p><b>Warning</b>: If the provided entity is a player, it is not
	 * guaranteed by the contract that said player is included in the
	 * resulting stream.
	 *
	 * @param entity the entity being tracked
	 * @return the players tracking the entity
	 * @throws IllegalArgumentException if the entity is not in a server world
	 */
	public static Collection<ServerPlayerEntity> tracking(Entity entity) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		ChunkManager manager = entity.getWorld().getChunkManager();

		if (manager instanceof ServerChunkManager) {
			ThreadedChunkManager chunkLoadingManager = ((ServerChunkManager) manager).delegate;
			EntityTrackerAccessor tracker = ((ServerChunkLoadingManagerAccessor) chunkLoadingManager).getEntityTrackers().get(entity.getId());

			// return an immutable collection to guard against accidental removals.
			if (tracker != null) {
				return tracker.getPlayersTracking()
						.stream().map(ServerPlayerConnection::getPlayer).collect(Collectors.toUnmodifiableSet());
			}

			return Collections.emptySet();
		}

		throw new IllegalArgumentException("Only supported on server worlds!");
	}

	/**
	 * Gets all players tracking a block entity in a server world.
	 *
	 * @param blockEntity the block entity
	 * @return the players tracking the block position
	 * @throws IllegalArgumentException if the block entity is not in a server world
	 */
	public static Collection<ServerPlayerEntity> tracking(BlockEntity blockEntity) {
		Objects.requireNonNull(blockEntity, "BlockEntity cannot be null");

		//noinspection ConstantConditions - IJ intrinsics don't know hasWorld == true will result in no null
		if (!blockEntity.hasWorld() || blockEntity.getWorld().isClient()) {
			throw new IllegalArgumentException("Only supported on server worlds!");
		}

		return tracking((ServerWorld) blockEntity.getWorld(), blockEntity.getPos());
	}

	/**
	 * Gets all players tracking a block position in a server world.
	 *
	 * @param world the server world
	 * @param pos   the block position
	 * @return the players tracking the block position
	 */
	public static Collection<ServerPlayerEntity> tracking(ServerWorld world, BlockPos pos) {
		Objects.requireNonNull(pos, "BlockPos cannot be null");

		return tracking(world, new ChunkPos(pos));
	}

	/**
	 * Gets all players around a position in a world.
	 *
	 * <p>The distance check is done in the three-dimensional space instead of in the horizontal plane.
	 *
	 * @param world  the world
	 * @param pos the position
	 * @param radius the maximum distance from the position in blocks
	 * @return the players around the position
	 */
	public static Collection<ServerPlayerEntity> around(ServerWorld world, Vec3d pos, double radius) {
		double radiusSq = radius * radius;

		return world(world)
				.stream()
				.filter((p) -> p.squaredDistanceTo(pos) <= radiusSq)
				.collect(Collectors.toList());
	}

	/**
	 * Gets all players around a position in a world.
	 *
	 * <p>The distance check is done in the three-dimensional space instead of in the horizontal plane.
	 *
	 * @param world  the world
	 * @param pos    the position (can be a block pos)
	 * @param radius the maximum distance from the position in blocks
	 * @return the players around the position
	 */
	public static Collection<ServerPlayerEntity> around(ServerWorld world, Vec3i pos, double radius) {
		double radiusSq = radius * radius;

		return world(world)
				.stream()
				.filter((p) -> p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= radiusSq)
				.collect(Collectors.toList());
	}

	private PlayerLookup() {
	}
}

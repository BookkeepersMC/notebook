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
package com.bookkeepersmc.notebook.impl.registry.sync;

import net.minecraft.registry.BuiltInRegistries;

import com.bookkeepersmc.api.ModInitializer;
import com.bookkeepersmc.notebook.api.event.registry.RegistryAttribute;
import com.bookkeepersmc.notebook.api.event.registry.RegistryAttributeHolder;
import com.bookkeepersmc.notebook.api.networking.v1.PayloadTypeRegistry;
import com.bookkeepersmc.notebook.api.networking.v1.ServerConfigurationConnectionEvents;
import com.bookkeepersmc.notebook.api.networking.v1.ServerConfigurationNetworking;
import com.bookkeepersmc.notebook.impl.registry.sync.packet.DirectRegistryPacketHandler;

public class RegistrySyncInit implements ModInitializer {
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationC2S().register(SyncCompletePayload.TYPE, SyncCompletePayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(DirectRegistryPacketHandler.Payload.TYPE, DirectRegistryPacketHandler.Payload.CODEC);

		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register(RegistrySyncManager::configureClient);
		ServerConfigurationNetworking.registerGlobalReceiver(SyncCompletePayload.TYPE, (payload, context) -> {
			context.networkHandler().completeTask(RegistrySyncManager.SyncConfigurationTask.KEY);
		});

		// Synced in PlaySoundS2CPacket.
		RegistryAttributeHolder.get(BuiltInRegistries.SOUND_EVENT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced with RegistryTagContainer from RegistryTagManager.
		RegistryAttributeHolder.get(BuiltInRegistries.FLUID)
				.addAttribute(RegistryAttribute.SYNCED);

		// StatusEffectInstance serialises with raw id.
		RegistryAttributeHolder.get(BuiltInRegistries.STATUS_EFFECT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in ChunkDeltaUpdateS2CPacket among other places, a pallet is used when saving.
		RegistryAttributeHolder.get(BuiltInRegistries.BLOCK)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in EntitySpawnS2CPacket and RegistryTagManager
		RegistryAttributeHolder.get(BuiltInRegistries.ENTITY_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in RegistryTagManager
		RegistryAttributeHolder.get(BuiltInRegistries.ITEM)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced via PacketCodecs.registry
		RegistryAttributeHolder.get(BuiltInRegistries.POTION)
				.addAttribute(RegistryAttribute.SYNCED);

		// Doesnt seem to be accessed apart from registering?
		RegistryAttributeHolder.get(BuiltInRegistries.CARVER);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.FEATURE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.BLOCK_STATE_PROVIDER_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.FOLIAGE_PLACER_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.TRUNK_PLACER_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.TREE_DECORATOR_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.FEATURE_SIZE_TYPE);

		// Synced in ParticleS2CPacket
		RegistryAttributeHolder.get(BuiltInRegistries.PARTICLE_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.BIOME_SOURCE);

		// Synced. Vanilla uses raw ids in BlockEntityUpdateS2CPacket, and mods use the Vanilla syncing since 1.18
		RegistryAttributeHolder.get(BuiltInRegistries.BLOCK_ENTITY_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced via PacketCodecs.registry
		RegistryAttributeHolder.get(BuiltInRegistries.CUSTOM_STAT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.CHUNK_STATUS);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_PIECE_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.RULE_TEST_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.POS_RULE_TEST_TYPE);

		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_PROCESSOR_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_POOL_ELEMENT_TYPE);

		// Uses a data tracker (and thus, raw IDs) to sync cat entities to the client
		RegistryAttributeHolder.get(BuiltInRegistries.CAT_VARIANT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Uses a data tracker (and thus, raw IDs) to sync frog entities to the client
		RegistryAttributeHolder.get(BuiltInRegistries.FROG_VARIANT)
				.addAttribute(RegistryAttribute.SYNCED);

		//  Uses the raw ID when syncing the command tree to the client
		RegistryAttributeHolder.get(BuiltInRegistries.COMMAND_ARGUMENT_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in OpenScreenS2CPacket
		RegistryAttributeHolder.get(BuiltInRegistries.SCREEN_HANDLER_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Does not seem to be serialised, only queried by id. Not synced
		RegistryAttributeHolder.get(BuiltInRegistries.RECIPE_TYPE);

		// Synced by id
		RegistryAttributeHolder.get(BuiltInRegistries.RECIPE_SERIALIZER)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID in 24w03a+
		RegistryAttributeHolder.get(BuiltInRegistries.ENTITY_ATTRIBUTE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced in StatisticsS2CPacket
		RegistryAttributeHolder.get(BuiltInRegistries.STAT_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID in TrackedDataHandlerRegistry.VILLAGER_DATA
		RegistryAttributeHolder.get(BuiltInRegistries.VILLAGER_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID in TrackedDataHandlerRegistry.VILLAGER_DATA
		RegistryAttributeHolder.get(BuiltInRegistries.VILLAGER_PROFESSION)
				.addAttribute(RegistryAttribute.SYNCED);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.POINT_OF_INTEREST_TYPE);

		// Serialised by string, doesnt seem to be synced
		RegistryAttributeHolder.get(BuiltInRegistries.MEMORY_MODULE_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.SENSOR_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.SCHEDULE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.ACTIVITY);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.LOOT_FUNCTION_TYPE);

		// Doesnt seem to be serialised or synced.
		RegistryAttributeHolder.get(BuiltInRegistries.LOOT_CONDITION_TYPE);

		// Synced in TagManager::toPacket/fromPacket -> TagGroup::serialize/deserialize
		RegistryAttributeHolder.get(BuiltInRegistries.GAME_EVENT)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID in its serialization code.
		RegistryAttributeHolder.get(BuiltInRegistries.NUMBER_FORMAT_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID.
		RegistryAttributeHolder.get(BuiltInRegistries.POSITION_SOURCE_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID.
		RegistryAttributeHolder.get(BuiltInRegistries.DATA_COMPONENT_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced by rawID.
		RegistryAttributeHolder.get(BuiltInRegistries.MAP_DECORATION_TYPE)
				.addAttribute(RegistryAttribute.SYNCED);

		// Synced via PacketCodecs.registry
		RegistryAttributeHolder.get(BuiltInRegistries.ARMOR_MATERIAL)
				.addAttribute(RegistryAttribute.SYNCED);
	}
}

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
package com.bookkeepersmc.notebook.impl.registry.sync.packet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import com.bookkeepersmc.notebook.api.networking.v1.ByteBufUtils;

public class DirectRegistryPacketHandler extends RegistryPacketHandler<DirectRegistryPacketHandler.Payload> {
	private static final int MAX_PAYLOAD_SIZE = Integer.getInteger("notebook.registry.direct.maxPayloadSize", 0x100000);

	@Nullable
	private PacketByteBuf combinedBuf;

	@Nullable
	private Map<Identifier, Object2IntMap<Identifier>> syncedRegistryMap;

	private boolean isPacketFinished = false;
	private int totalPacketReceived = 0;

	@Override
	public CustomPayload.Id<DirectRegistryPacketHandler.Payload> getPacketId() {
		return Payload.TYPE;
	}

	@Override
	public void sendPacket(Consumer<DirectRegistryPacketHandler.Payload> sender, Map<Identifier, Object2IntMap<Identifier>> registryMap) {
		PacketByteBuf buf = ByteBufUtils.create();

		// Group registry ids with same namespace.
		Map<String, List<Identifier>> regNamespaceGroups = registryMap.keySet().stream()
				.collect(Collectors.groupingBy(Identifier::getNamespace));

		buf.writeVarInt(regNamespaceGroups.size());

		regNamespaceGroups.forEach((regNamespace, regIds) -> {
			buf.writeString(optimizeNamespace(regNamespace));
			buf.writeVarInt(regIds.size());

			for (Identifier regId : regIds) {
				buf.writeString(regId.getPath());

				Object2IntMap<Identifier> idMap = registryMap.get(regId);

				// Sort object ids by its namespace. We use linked map here to keep the original namespace ordering.
				Map<String, List<Object2IntMap.Entry<Identifier>>> idNamespaceGroups = idMap.object2IntEntrySet().stream()
						.collect(Collectors.groupingBy(e -> e.getKey().getNamespace(), LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));

				buf.writeVarInt(idNamespaceGroups.size());

				int lastBulkLastRawId = 0;

				for (Map.Entry<String, List<Object2IntMap.Entry<Identifier>>> idNamespaceEntry : idNamespaceGroups.entrySet()) {
					// Make sure the ids are sorted by its raw id.
					List<Object2IntMap.Entry<Identifier>> idPairs = idNamespaceEntry.getValue();
					idPairs.sort(Comparator.comparingInt(Object2IntMap.Entry::getIntValue));

					// Group consecutive raw ids together.
					List<List<Object2IntMap.Entry<Identifier>>> bulks = new ArrayList<>();

					Iterator<Object2IntMap.Entry<Identifier>> idPairIter = idPairs.iterator();
					List<Object2IntMap.Entry<Identifier>> currentBulk = new ArrayList<>();
					Object2IntMap.Entry<Identifier> currentPair = idPairIter.next();
					currentBulk.add(currentPair);

					while (idPairIter.hasNext()) {
						currentPair = idPairIter.next();

						if (currentBulk.get(currentBulk.size() - 1).getIntValue() + 1 != currentPair.getIntValue()) {
							bulks.add(currentBulk);
							currentBulk = new ArrayList<>();
						}

						currentBulk.add(currentPair);
					}

					bulks.add(currentBulk);

					buf.writeString(optimizeNamespace(idNamespaceEntry.getKey()));
					buf.writeVarInt(bulks.size());

					for (List<Object2IntMap.Entry<Identifier>> bulk : bulks) {
						int firstRawId = bulk.get(0).getIntValue();
						int bulkRawIdStartDiff = firstRawId - lastBulkLastRawId;

						buf.writeVarInt(bulkRawIdStartDiff);
						buf.writeVarInt(bulk.size());

						for (Object2IntMap.Entry<Identifier> idPair : bulk) {
							buf.writeString(idPair.getKey().getPath());

							lastBulkLastRawId = idPair.getIntValue();
						}
					}
				}
			}
		});

		// Split the packet to multiple MAX_PAYLOAD_SIZEd buffers.
		int readableBytes = buf.readableBytes();
		int sliceIndex = 0;

		while (sliceIndex < readableBytes) {
			int sliceSize = Math.min(readableBytes - sliceIndex, MAX_PAYLOAD_SIZE);
			PacketByteBuf slicedBuf = ByteBufUtils.slice(buf, sliceIndex, sliceSize);
			sender.accept(createPayload(slicedBuf));
			sliceIndex += sliceSize;
		}

		// Send an empty buffer to mark the end of the split.
		sender.accept(createPayload(ByteBufUtils.empty()));
	}

	@Override
	public void receivePayload(Payload payload) {
		Preconditions.checkState(!isPacketFinished);
		totalPacketReceived++;

		if (combinedBuf == null) {
			combinedBuf = ByteBufUtils.create();
		}

		byte[] data = payload.data();

		if (data.length != 0) {
			combinedBuf.writeBytes(data);
			return;
		}

		isPacketFinished = true;

		computeBufSize(combinedBuf);
		syncedRegistryMap = new LinkedHashMap<>();
		int regNamespaceGroupAmount = combinedBuf.readVarInt();

		for (int i = 0; i < regNamespaceGroupAmount; i++) {
			String regNamespace = unoptimizeNamespace(combinedBuf.readString());
			int regNamespaceGroupLength = combinedBuf.readVarInt();

			for (int j = 0; j < regNamespaceGroupLength; j++) {
				String regPath = combinedBuf.readString();
				Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
				int idNamespaceGroupAmount = combinedBuf.readVarInt();

				int lastBulkLastRawId = 0;

				for (int k = 0; k < idNamespaceGroupAmount; k++) {
					String idNamespace = unoptimizeNamespace(combinedBuf.readString());
					int rawIdBulkAmount = combinedBuf.readVarInt();

					for (int l = 0; l < rawIdBulkAmount; l++) {
						int bulkRawIdStartDiff = combinedBuf.readVarInt();
						int bulkSize = combinedBuf.readVarInt();

						int currentRawId = (lastBulkLastRawId + bulkRawIdStartDiff) - 1;

						for (int m = 0; m < bulkSize; m++) {
							currentRawId++;
							String idPath = combinedBuf.readString();
							idMap.put(Identifier.of(idNamespace, idPath), currentRawId);
						}

						lastBulkLastRawId = currentRawId;
					}
				}

				syncedRegistryMap.put(Identifier.of(regNamespace, regPath), idMap);
			}
		}

		combinedBuf.release();
		combinedBuf = null;
	}

	@Override
	public boolean isPacketFinished() {
		return isPacketFinished;
	}

	@Override
	public int getTotalPacketReceived() {
		Preconditions.checkState(isPacketFinished);
		return totalPacketReceived;
	}

	@Override
	@Nullable
	public Map<Identifier, Object2IntMap<Identifier>> getSyncedRegistryMap() {
		Preconditions.checkState(isPacketFinished);
		Map<Identifier, Object2IntMap<Identifier>> map = syncedRegistryMap;
		isPacketFinished = false;
		totalPacketReceived = 0;
		syncedRegistryMap = null;
		return map;
	}

	private DirectRegistryPacketHandler.Payload createPayload(PacketByteBuf buf) {
		if (buf.readableBytes() == 0) {
			return new Payload(new byte[0]);
		}

		return new Payload(buf.array());
	}

	private static String optimizeNamespace(String namespace) {
		return namespace.equals(Identifier.DEFAULT_NAMESPACE) ? "" : namespace;
	}

	private static String unoptimizeNamespace(String namespace) {
		return namespace.isEmpty() ? Identifier.DEFAULT_NAMESPACE : namespace;
	}

	public record Payload(byte[] data) implements RegistrySyncPayload {
		public static CustomPayload.Id<Payload> TYPE = new Id<>(Identifier.of("notebook", "registry/sync/direct"));
		public static PacketCodec<PacketByteBuf, Payload> CODEC = CustomPayload.create(Payload::write, Payload::new);

		Payload(PacketByteBuf buf) {
			this(readAllBytes(buf));
		}

		private void write(PacketByteBuf buf) {
			buf.writeBytes(data);
		}

		private static byte[] readAllBytes(PacketByteBuf buf) {
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			return bytes;
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return TYPE;
		}
	}
}

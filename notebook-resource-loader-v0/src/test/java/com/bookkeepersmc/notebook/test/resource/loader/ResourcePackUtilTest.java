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
package com.bookkeepersmc.notebook.test.resource.loader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;

import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackCreator;
import com.bookkeepersmc.notebook.impl.resource.loader.ModResourcePackUtil;
import com.bookkeepersmc.notebook.impl.resource.loader.NotebookResourcePackProfile;

public class ResourcePackUtilTest {
	private static final Gson GSON = new Gson();

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testRefreshAutoEnabledPacks() {
		// Vanilla uses tree map, and we test the behavior
		Map<String, Pack> profiles = new TreeMap<>();
		Map<String, Pack> modAProfiles = new TreeMap<>();
		Map<String, Pack> modBProfiles = new TreeMap<>();
		Map<String, Pack> allProfiles = new TreeMap<>();
		Pack vanilla = mockProfile(profiles, "vanilla", null);
		Pack fabric = mockProfile(profiles, ModResourcePackCreator.NOTEBOOK, null);
		Pack modA = mockProfile(modAProfiles, "mod_a", ModResourcePackCreator.BASE_PARENT);
		Pack modAProg = mockProfile(modAProfiles, "mod_a_programmer_art", ModResourcePackCreator.PROGRAMMER_ART_PARENT);
		Pack modAHigh = mockProfile(modAProfiles, "mod_a_high_contrast", ModResourcePackCreator.HIGH_CONTRAST_PARENT);
		Pack modB = mockProfile(modBProfiles, "mod_b", ModResourcePackCreator.BASE_PARENT);
		Pack modBProg = mockProfile(modBProfiles, "mod_b_programmer_art", ModResourcePackCreator.PROGRAMMER_ART_PARENT);
		Pack modBHigh = mockProfile(modBProfiles, "mod_b_high_contrast", ModResourcePackCreator.HIGH_CONTRAST_PARENT);
		Pack programmerArt = mockProfile(profiles, "programmer_art", null);
		Pack highContrast = mockProfile(profiles, "high_contrast", null);
		Pack userPackA = mockProfile(profiles, "user_pack_a", null);
		Pack userPackB = mockProfile(profiles, "user_pack_b", null);
		modAProfiles.putAll(profiles);
		modBProfiles.putAll(profiles);
		allProfiles.putAll(modAProfiles);
		allProfiles.putAll(modBProfiles);

		testRefreshAutoEnabledPacks(
				profiles,
				List.of(vanilla, fabric),
				List.of(vanilla, fabric),
				"keep (no mods)"
		);
		testRefreshAutoEnabledPacks(
				profiles,
				List.of(vanilla, fabric, userPackA),
				List.of(vanilla, fabric, userPackA),
				"keep (no mods, keep user pack)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA),
				List.of(vanilla, fabric, modA),
				"keep (mod A only)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"keep (programmer_art)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg),
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg),
				"keep (mod A and mod B, programmer_art)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg, highContrast, modAHigh, modBHigh),
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg, highContrast, modAHigh, modBHigh),
				"keep (mod A and mod B, both)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modA, modB, highContrast, modAHigh, modBHigh, programmerArt, modAProg, modBProg),
				List.of(vanilla, fabric, modA, modB, highContrast, modAHigh, modBHigh, programmerArt, modAProg, modBProg),
				"keep (remembers programmer_art-high_contrast order)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric),
				List.of(vanilla, fabric, modA),
				"fix (adding missing mods)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, userPackA),
				List.of(vanilla, fabric, modA, modB, userPackA),
				"fix (adding missing mods at the right place)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modB, modA),
				List.of(vanilla, fabric, modA, modB),
				"fix (mod A and B, sorting)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, userPackB, modA, userPackA),
				List.of(vanilla, fabric, modA, userPackB, userPackA),
				"fix (user pack goes last)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"fix (adding 1 met dep)"
		);
		testRefreshAutoEnabledPacks(
				modBProfiles,
				List.of(vanilla, fabric, modB, highContrast),
				List.of(vanilla, fabric, modB, highContrast, modBHigh),
				"fix (adding 1 met dep, part 2)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, highContrast),
				List.of(vanilla, fabric, modA, programmerArt, modAProg, highContrast, modAHigh),
				"fix (adding 2 met deps)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, modAProg, highContrast),
				List.of(vanilla, fabric, modA, programmerArt, modAProg, highContrast, modAHigh),
				"fix (adding 2 met deps + preexisting)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, modAProg, modAHigh),
				List.of(vanilla, fabric, modA),
				"fix (removing 2 unmet deps)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, modAProg, modAHigh),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"fix (removing 1 unmet dep)"
		);
		testRefreshAutoEnabledPacks(
				modBProfiles,
				List.of(vanilla, fabric, modB, highContrast, modBProg, modBHigh),
				List.of(vanilla, fabric, modB, highContrast, modBHigh),
				"fix (removing 1 unmet dep, part 2)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modAProg, programmerArt, modA),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"reorder (bundled comes just after parents)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modAProg, userPackA, programmerArt, modA, userPackB),
				List.of(vanilla, fabric, modA, userPackA, programmerArt, modAProg, userPackB),
				"reorder (keep user pack order)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, userPackB, modA, programmerArt, userPackA, modAProg),
				List.of(vanilla, fabric, modA, userPackB, programmerArt, modAProg, userPackA),
				"reorder (no user pack between parent-bundled)"
		);
	}

	private Pack mockProfile(Map<String, Pack> profiles, String id, @Nullable Predicate<Set<String>> parents) {
		Pack profile = new Pack(
				new PackLocationInfo(
						id,
						null,
						null,
						Optional.empty()
				),
				null,
				null,
				new PackSelectionConfig(
						false,
						null,
						false)
		);

		if (parents != null) ((NotebookResourcePackProfile) profile).notebook_setParentsPredicate(parents);

		profiles.put(id, profile);
		return profile;
	}

	private void testRefreshAutoEnabledPacks(Map<String, Pack> profiles, List<Pack> before, List<Pack> after, String reason) {
		List<Pack> processed = new ArrayList<>(before);
		ModResourcePackUtil.refreshAutoEnabledPacks(processed, profiles);
		assertEquals(
				after.stream().map(Pack::getId).toList(),
				processed.stream().map(Pack::getId).toList(),
				() -> "Testing %s; input %s".formatted(reason, before.stream().map(Pack::getId).toList())
		);
	}

	@Test
	void testSerializeMetadata() {
		// Test various metadata serialization issues (#2407)
		testMetadataSerialization("");
		testMetadataSerialization("Quotes: \"\" \"");
		testMetadataSerialization("Backslash: \\ \\\\");
	}

	private void testMetadataSerialization(String description) throws JsonParseException {
		String metadata = ModResourcePackUtil.serializeMetadata(1, description);
		JsonObject json = assertDoesNotThrow(() -> GSON.fromJson(metadata, JsonObject.class), () -> "Failed to serialize " + description);

		String parsedDescription = json.get("pack").getAsJsonObject().get("description").getAsString();
		assertEquals(description, parsedDescription, "Parsed description differs from original one");
	}
}

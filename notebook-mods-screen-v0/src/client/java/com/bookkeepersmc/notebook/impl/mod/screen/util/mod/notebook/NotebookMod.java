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
package com.bookkeepersmc.notebook.impl.mod.screen.util.mod.notebook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImageBackedTexture;

import com.bookkeepersmc.loader.api.ModContainer;
import com.bookkeepersmc.loader.api.NotebookLoader;
import com.bookkeepersmc.loader.api.metadata.*;
import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateChecker;
import com.bookkeepersmc.notebook.api.mod.screen.v0.UpdateInfo;
import com.bookkeepersmc.notebook.impl.mod.screen.NotebookModScreen;
import com.bookkeepersmc.notebook.impl.mod.screen.config.ModScreenConfig;
import com.bookkeepersmc.notebook.impl.mod.screen.util.OptionalUtil;
import com.bookkeepersmc.notebook.impl.mod.screen.util.VersionUtil;
import com.bookkeepersmc.notebook.impl.mod.screen.util.mod.Mod;

public class NotebookMod implements Mod {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | FabricMod");

	protected final ModContainer container;
	protected final ModMetadata metadata;

	protected final ModMenuData modMenuData;

	protected final Set<Badge> badges;

	protected final Map<String, String> links = new HashMap<>();

	protected @Nullable UpdateChecker updateChecker = null;
	protected @Nullable UpdateInfo updateInfo = null;

	protected boolean defaultIconWarning = true;

	protected boolean allowsUpdateChecks = true;

	protected boolean childHasUpdate = false;

	public NotebookMod(ModContainer modContainer, Set<String> modpackMods) {
		this.container = modContainer;
		this.metadata = modContainer.getMetadata();

		String id = metadata.getId();

		if ("minecraft".equals(id) || "java".equals(id)) {
			allowsUpdateChecks = false;
		}

		/* Load modern mod menu custom value data */
		Optional<String> parentId = Optional.empty();
		ModMenuData.DummyParentData parentData = null;
		Set<String> badgeNames = new HashSet<>();
		CustomValue modMenuValue = metadata.getCustomValue("modmenu");
		if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
			CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();
			CustomValue parentCv = modMenuObject.get("parent");
			if (parentCv != null) {
				if (parentCv.getType() == CustomValue.CvType.STRING) {
					parentId = Optional.of(parentCv.getAsString());
				} else if (parentCv.getType() == CustomValue.CvType.OBJECT) {
					try {
						CustomValue.CvObject parentObj = parentCv.getAsObject();
						parentId = CustomValueUtil.getString("id", parentObj);
						parentData = new ModMenuData.DummyParentData(
							parentId.orElseThrow(() -> new RuntimeException("Parent object lacks an id")),
							CustomValueUtil.getString("name", parentObj),
							CustomValueUtil.getString("description", parentObj),
							CustomValueUtil.getString("icon", parentObj),
							CustomValueUtil.getStringSet("badges", parentObj).orElse(new HashSet<>())
						);
						if (parentId.orElse("").equals(id)) {
							parentId = Optional.empty();
							parentData = null;
							throw new RuntimeException("Mod declared itself as its own parent");
						}
					} catch (Throwable t) {
						LOGGER.error("Error loading parent data from mod: " + id, t);
					}
				}
			}
			badgeNames.addAll(CustomValueUtil.getStringSet("badges", modMenuObject).orElse(new HashSet<>()));
			links.putAll(CustomValueUtil.getStringMap("links", modMenuObject).orElse(new HashMap<>()));
			allowsUpdateChecks = CustomValueUtil.getBoolean("update_checker", modMenuObject).orElse(true);
		}
		this.modMenuData = new ModMenuData(badgeNames, parentId, parentData, id);

		/* Hardcode parents and badges for Fabric API & Fabric Loader */
		if ((id.startsWith("notebook") && !id.equals("notebook-mods-screen-v0")) && metadata.containsCustomValue("notebook-api:module-lifecycle")) {
			if (NotebookLoader.getInstance().isModLoaded("notebook-api") || !NotebookLoader.getInstance()
				.isModLoaded("notebook")) {
				modMenuData.fillParentIfEmpty("notebook-api");
			} else {
				modMenuData.fillParentIfEmpty("notebook");
			}
			modMenuData.badges.add(Badge.LIBRARY);
		}
		if (id.startsWith("notebook") && (id.equals("NotebookLoader") || metadata.getProvides()
			.contains("NotebookLoader") || id.equals("notebook") || id.equals("notebook-api") || metadata.getProvides()
			.contains("notebook") || metadata.getProvides()
			.contains("notebook-api")) && !id.equals("notebook-mods-screen-v0")) {
			modMenuData.badges.add(Badge.LIBRARY);
		}

		/* Add additional badges */
		this.badges = modMenuData.badges;
		if (this.metadata.getEnvironment() == ModEnvironment.CLIENT) {
			badges.add(Badge.CLIENT);
		}
		if (OptionalUtil.isPresentAndTrue(CustomValueUtil.getBoolean(
			"fabric-loom:generated",
			metadata
		)) || "java".equals(id)) {
			badges.add(Badge.LIBRARY);
		}
		if ("deprecated".equals(CustomValueUtil.getString("fabric-api:module-lifecycle", metadata).orElse(null))) {
			badges.add(Badge.DEPRECATED);
		}
		if (metadata.containsCustomValue("patchwork:patcherMeta")) {
			badges.add(Badge.PATCHWORK_FORGE);
		}
		if (modpackMods.contains(getId()) && !"builtin".equals(this.metadata.getType())) {
			badges.add(Badge.MODPACK);
		}
		if ("minecraft".equals(getId())) {
			badges.add(Badge.MINECRAFT);
		}
	}

	public @NotNull ModContainer getContainer() {
		return container;
	}

	@Override
	public @NotNull String getId() {
		return metadata.getId();
	}

	@Override
	public @NotNull String getName() {
		return metadata.getName();
	}

	@Override
	public @NotNull NativeImageBackedTexture getIcon(IconHandler iconHandler, int i) {
		String iconSourceId = getId();
		String iconPath = metadata.getIconPath(i).orElse("assets/" + getId() + "/icon.png");
		if ("minecraft".equals(getId())) {
			iconSourceId = NotebookModScreen.MOD_ID;
			iconPath = "assets/" + NotebookModScreen.MOD_ID + "/minecraft_icon.png";
		} else if ("java".equals(getId())) {
			iconSourceId = NotebookModScreen.MOD_ID;
			iconPath = "assets/" + NotebookModScreen.MOD_ID + "/java_icon.png";
		}
		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = NotebookLoader.getInstance()
			.getModContainer(iconSourceId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		NativeImageBackedTexture icon = iconHandler.createIcon(iconSource, iconPath);
		if (icon == null) {
			if (defaultIconWarning) {
				LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", metadata.getId());
				defaultIconWarning = false;
			}
			return iconHandler.createIcon(
				NotebookLoader.getInstance()
					.getModContainer(NotebookModScreen.MOD_ID)
					.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + NotebookModScreen.MOD_ID)),
				"assets/" + NotebookModScreen.MOD_ID + "/unknown_icon.png"
			);
		}
		return icon;
	}

	@Override
	public @NotNull String getDescription() {
		return metadata.getDescription();
	}

	@Override
	public @NotNull String getTranslatedDescription() {
		var description = Mod.super.getTranslatedDescription();
		if (getId().equals("java")) {
			description = description + "\n" + I18n.translate("modscreen.javaDistributionName", getName());
		}
		return description;
	}

	@Override
	public @NotNull String getVersion() {
		if ("java".equals(getId())) {
			return System.getProperty("java.version");
		}
		return metadata.getVersion().getFriendlyString();
	}

	public @NotNull String getPrefixedVersion() {
		return VersionUtil.getPrefixedVersion(getVersion());
	}

	@Override
	public @NotNull List<String> getAuthors() {
		List<String> authors = metadata.getAuthors().stream().map(Person::getName).collect(Collectors.toList());
		if (authors.isEmpty()) {
			if ("minecraft".equals(getId())) {
				return Lists.newArrayList("Mojang Studios");
			} else if ("java".equals(getId())) {
				return Lists.newArrayList(System.getProperty("java.vendor"));
			}
		}
		return authors;
	}

	@Override
	public @NotNull Map<String, Collection<String>> getContributors() {
		Map<String, Collection<String>> contributors = new LinkedHashMap<>();

		for (var contributor : this.metadata.getContributors()) {
			contributors.put(contributor.getName(), List.of("Contributor"));
		}

		return contributors;
	}

	@Override
	public @NotNull SortedMap<String, Set<String>> getCredits() {
		SortedMap<String, Set<String>> credits = new TreeMap<>();

		var authors = this.getAuthors();
		var contributors = this.getContributors();

		for (var author : authors) {
			contributors.put(author, List.of("Author"));
		}

		for (var contributor : contributors.entrySet()) {
			for (var role : contributor.getValue()) {
				credits.computeIfAbsent(role, key -> new LinkedHashSet<>());
				credits.get(role).add(contributor.getKey());
			}
		}

		return credits;
	}

	@Override
	public @NotNull Set<Badge> getBadges() {
		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		if ("minecraft".equals(getId())) {
			return "https://www.minecraft.net/";
		} else if ("java".equals(getId())) {
			return System.getProperty("java.vendor.url");
		}
		return metadata.getContact().get("homepage").orElse(null);
	}

	@Override
	public @Nullable String getIssueTracker() {
		if ("minecraft".equals(getId())) {
			return "https://aka.ms/snapshotbugs?ref=game";
		}
		return metadata.getContact().get("issues").orElse(null);
	}

	@Override
	public @Nullable String getSource() {
		return metadata.getContact().get("sources").orElse(null);
	}

	@Override
	public @Nullable String getParent() {
		return modMenuData.parent.orElse(null);
	}

	@Override
	public @NotNull Set<String> getLicense() {
		if ("minecraft".equals(getId())) {
			return Sets.newHashSet("Minecraft EULA");
		}
		return Sets.newHashSet(metadata.getLicense());
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return links;
	}

	@Override
	public boolean isReal() {
		return true;
	}

	@Override
	public boolean allowsUpdateChecks() {
		if (ModScreenConfig.DISABLE_UPDATE_CHECKER.getValue().contains(this.getId())) {
			return false;
		}

		return this.allowsUpdateChecks;
	}

	@Override
	public @Nullable UpdateChecker getUpdateChecker() {
		return updateChecker;
	}

	@Override
	public void setUpdateChecker(@Nullable UpdateChecker updateChecker) {
		this.updateChecker = updateChecker;
	}

	@Override
	public @Nullable UpdateInfo getUpdateInfo() {
		return updateInfo;
	}

	@Override
	public void setUpdateInfo(@Nullable UpdateInfo updateInfo) {
		this.updateInfo = updateInfo;
		String parent = getParent();
		if (parent != null && updateInfo != null && updateInfo.isUpdateAvailable()) {
			NotebookModScreen.MODS.get(parent).setChildHasUpdate();
		}
	}

	public ModMenuData getModMenuData() {
		return modMenuData;
	}

	public @Nullable String getSha512Hash() throws IOException {
		if (container.getContainingMod().isEmpty() && container.getOrigin().getKind() == ModOrigin.Kind.PATH) {
			List<Path> paths = container.getOrigin().getPaths();
			var fileOptional = paths.stream()
				.filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
				.findFirst();
			if (fileOptional.isPresent()) {
				var file = fileOptional.get().toFile();
				if (file.isFile()) {
					return Files.asByteSource(file).hash(Hashing.sha512()).toString();
				}
			}
		}
		return null;
	}

	@Override
	public boolean getChildHasUpdate() {
		return childHasUpdate;
	}

	@Override
	public void setChildHasUpdate() {
		this.childHasUpdate = true;
	}

	@Override
	public boolean isHidden() {
		return ModScreenConfig.HIDDEN_MODS.getValue().contains(this.getId());
	}

	static class ModMenuData {
		private final Set<Badge> badges;
		private Optional<String> parent;
		private @Nullable
		final DummyParentData dummyParentData;

		public ModMenuData(Set<String> badges, Optional<String> parent, DummyParentData dummyParentData, String id) {
			this.badges = Badge.convert(badges, id);
			this.parent = parent;
			this.dummyParentData = dummyParentData;
		}

		public Set<Badge> getBadges() {
			return badges;
		}

		public Optional<String> getParent() {
			return parent;
		}

		public @Nullable DummyParentData getDummyParentData() {
			return dummyParentData;
		}

		public void addClientBadge(boolean add) {
			if (add) {
				badges.add(Badge.CLIENT);
			}
		}

		public void addLibraryBadge(boolean add) {
			if (add) {
				badges.add(Badge.LIBRARY);
			}
		}

		public void fillParentIfEmpty(String parent) {
			if (!this.parent.isPresent()) {
				this.parent = Optional.of(parent);
			}
		}

		public static class DummyParentData {
			private final String id;
			private final Optional<String> name;
			private final Optional<String> description;
			private final Optional<String> icon;
			private final Set<Badge> badges;

			public DummyParentData(
				String id,
				Optional<String> name,
				Optional<String> description,
				Optional<String> icon,
				Set<String> badges
			) {
				this.id = id;
				this.name = name;
				this.description = description;
				this.icon = icon;
				this.badges = Badge.convert(badges, id);
			}

			public String getId() {
				return id;
			}

			public Optional<String> getName() {
				return name;
			}

			public Optional<String> getDescription() {
				return description;
			}

			public Optional<String> getIcon() {
				return icon;
			}

			public Set<Badge> getBadges() {
				return badges;
			}
		}
	}
}

pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven("https://bookkeepersmc.github.io/m2/")

		maven("https://maven.fabricmc.net/")
	}
}

rootProject.name = "notebook-api"

include("notebook-api-base")
include("notebook-common-tags-v1")
include("notebook-datagen-api-v1")
include("notebook-keybind-api-v1")
include("notebook-lifecycle-events-v1")
include("notebook-mods-screen-v0")
include("notebook-networking-api-v1")
include("notebook-recipe-api-v1")
include("notebook-registry-sync-v0")
include("notebook-resource-conditions-v1")
include("notebook-resource-loader-v0")
include("notebook-screen-api-v1")



import net.fabricmc.loom.util.gradle.SourceSetHelper
import org.ajoberstar.grgit.Grgit
import org.apache.commons.codec.digest.DigestUtils
import java.util.stream.Collectors

plugins {
    id("java-library")
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("jacoco")
    id("notebook-loom") version "1.0.6"
    id("com.diffplug.spotless") version "6.20.0"
    id("org.ajoberstar.grgit") version "3.1.0"
    id("me.modmuss50.remotesign") version "0.4.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.4.5"
}

val env: Map<String, String> = System.getenv()
val checkIfLocal = if (env["GITHUB_RUN_NUMBER"].isNullOrBlank()) "" else "local-"

val mcVersion: String by project
val loaderVersion: String by project

version = "${project.version}+$checkIfLocal" + getBranch()
logger.lifecycle("Building: $version")

val META_PROJECTS: List<String> = listOf(
    "deprecated",
    "notebook-api-bom",
    "notebook-api-catalog"
)
ext["getSubprojectVersion"] = object : groovy.lang.Closure<Unit>(this) {
    fun doCall(project: Project): String {
        return getSubprojectVersion(project)
    }
}

ext["moduleDependencies"] = object : groovy.lang.Closure<Unit>(this) {
    fun doCall(project: Project, depNames: List<String>) {
        moduleDependencies(project, depNames)
    }
}

ext["testDependencies"] = object : groovy.lang.Closure<Unit>(this) {
    fun doCall(project: Project, depNames: List<String>) {
        testDependencies(project, depNames)
    }
}

val DEV_ONLY_MODULES: List<String> = listOf(
    "notebook-gametest-api-v1"
)

val debugArgs = listOf(
    "-enableassertions",
    "-Dmixin.debug.verify=true",
    // "-Dmixin.debug.strict=true",
    "-Dmixin.debug.countInjections=true"
)

fun getSubprojectVersion(project: Project): String {
    val version = project.properties["${project.name}-version"] ?: throw NullPointerException("Could not find version for ${project.name}")

    if (grgit == null) {
        return "$version+nogit"
    }

    val latestCommits = grgit.log(mapOf("paths" to listOf(project.name), "maxCommits" to 1))

    if (latestCommits.isEmpty()) {
        return "$version+uncommited"
    }

    return "$version+${latestCommits[0].id.substring(0, 8)}${DigestUtils.sha256Hex(mcVersion).substring(0, 2)}"
}

val grgit = Grgit.open(mapOf("currentDir" to project.rootDir))

fun getBranch(): String {
    val env: Map<String, String> = System.getenv()
    if (env["GITHUB_REF"] != null) {
        val branch = env["GITHUB_REF"]!!
        return branch.substring(branch.lastIndexOf("/") + 1)
    }

    if (project == rootProject) {
        return "$version"
    }

    if (grgit == null) {
        return "unknown"
    }

    val branch = grgit.branch.current().name
    return branch.substring(branch.lastIndexOf("/") + 1)
}

fun moduleDependencies(project: Project, depNames: List<String>) {
    val deps = depNames.map { project.dependencies.project(path = ":$it", configuration = "namedElements") }
    val clientOutputs = depNames.map { findProject(":$it")!!.sourceSets.getByName("client").output }

    project.dependencies {
        deps.forEach { api(it) }
        clientOutputs.forEach { "clientImplementation"(it) }
    }
}

fun testDependencies(project: Project, depNames: List<String>) {
    val deps = depNames.map { project.dependencies.project(path = ":$it", configuration = "namedElements") }
    val clientOutputs = depNames.map { findProject(":$it")!!.sourceSets.getByName("client").output }

    project.dependencies {
        deps.forEach { "testmodImplementation"(it) }
        clientOutputs.forEach { "testmodClientImplementation"(it) }
    }
}

allprojects {

    group = "com.bookkeepersmc.notebook-api"

    apply(plugin = "maven-publish")
    apply(plugin = "me.modmuss50.remotesign")

    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }

    publishing {
        setupRepositories(repositories)
    }

    if (META_PROJECTS.contains(name)) {
        return@allprojects
    }

    apply(plugin = "java-library")
    apply(plugin = "notebook-loom")
    apply(plugin = "com.diffplug.spotless")

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
    }

    java {
        // Must be added before the split source sets are setup.
        withSourcesJar()
    }

    loom {
        splitEnvironmentSourceSets()
        mixin {
            useLegacyMixinAp.set(true)
        }
    }

    sourceSets {
        create("testmod") {
            compileClasspath += named("main").get().compileClasspath
            runtimeClasspath += named("main").get().runtimeClasspath
        }

        create("testmodClient") {
            compileClasspath += named("main").get().compileClasspath
            runtimeClasspath += named("main").get().runtimeClasspath
            compileClasspath += named("client").get().compileClasspath
            runtimeClasspath += named("client").get().runtimeClasspath

            compileClasspath += named("testmod").get().compileClasspath
            runtimeClasspath += named("testmod").get().runtimeClasspath
        }

        getByName("test") {
            compileClasspath += named("testmodClient").get().compileClasspath
            runtimeClasspath += named("testmodClient").get().runtimeClasspath
        }
    }

    loom {
        runtimeOnlyLog4j = true

        runs {
            create("testmodClient") {
                client()
                ideConfigGenerated(project.rootProject == project)
                name = "Testmod Client"
                source(sourceSets.getByName("testmodClient"))
            }
            create("testmodServer") {
                server()
                ideConfigGenerated(project.rootProject == project)
                name = "Testmod Server"
                source(sourceSets.getByName("testmod"))
            }
        }
    }

    loom.runs.configureEach {
        vmArgs(debugArgs)
    }

    allprojects.forEach { p ->
        if (META_PROJECTS.contains(project.name)) {
            return@forEach
        }

        loom.mods.register(p.name) {
            sourceSet(p.sourceSets.getByName("testmod"))
            sourceSet(p.sourceSets.getByName("testmodClient"))
        }
    }

    dependencies {
        minecraft("com.mojang:minecraft:$mcVersion")
        mappings(loom.officialMojangMappings())
        modApi("com.bookkeepersmc:notebook-loader:$loaderVersion")

        "testmodImplementation"(sourceSets.named("main").get().output)
        "testmodClientImplementation"(sourceSets.named("main").get().output)
        "testmodClientImplementation"(sourceSets.named("client").get().output)
        "testmodClientImplementation"(sourceSets.named("testmod").get().output)

        testImplementation("com.bookkeepersmc:notebook-loader-junit:$loaderVersion")
        testImplementation(sourceSets.named("testmodClient").get().output)
        testImplementation("org.mockito:mockito-core:5.4.0")
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        afterEvaluate {
            val classPathGroups = loom.mods.stream()
                .map { modSettings ->
                    SourceSetHelper.getClasspath(modSettings, project).stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.joining(File.pathSeparator))
                }
                .collect(Collectors.joining(File.pathSeparator + File.pathSeparator))

            systemProperty("notebook.classPathGroups", classPathGroups)
        }
    }

    tasks.withType<ProcessResources>().configureEach {
        inputs.property("version", project.version)

        filesMatching("notebook.mod.json") {
            expand("version" to project.version)
        }
    }

    spotless {
        java {
            removeUnusedImports()
            importOrder("java", "javax", "", "net.minecraft", "com.bookkeepersmc")
            indentWithTabs()
            trimTrailingWhitespace()
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    tasks.register("generateResources") {
        group = "notebook"
    }

    val testmodJar by tasks.registering(Jar::class) {
        from(sourceSets.named("testmod").get().output)
        from(sourceSets.named("testmodClient").get().output)
        destinationDirectory.set(File(project.buildDir, "libs"))
        archiveClassifier.set("testmod")
    }

    tasks {
        jar {
            from(rootProject.file("LICENSE")) {
                rename { "${it}-${project.base.archivesName.get()}" }
            }
        }
    }

    if (file("src/client").exists() && !file("src/main").exists()) {
        tasks.remapJar {
            additionalClientOnlyEntries.add("LICENSE-${project.base.archivesName.get()}")
        }

        tasks.remapSourcesJar {
            additionalClientOnlyEntries.add("LICENSE-${project.base.archivesName.get()}")
        }
    }

    val remapTestmodJar by tasks.registering(net.fabricmc.loom.task.RemapJarTask::class) {
        dependsOn(testmodJar)
        input.set(testmodJar.get().archiveFile)
        archiveClassifier.set("testmod")
        addNestedDependencies.set(false)
        includesClientOnlyClasses.set(true)
        clientOnlySourceSetName.set(sourceSets.getByName("testmodClient").name)
    }
    tasks.build.get().dependsOn(remapTestmodJar)

    val validateMixinNames by tasks.registering(net.fabricmc.loom.task.ValidateMixinNameTask::class) {
        source(sourceSets.getByName("main").output)
        source(sourceSets.getByName("client").output)
        source(sourceSets.getByName("testmod").output)
    }
}

tasks.getByName<net.fabricmc.loom.task.RemapJarTask>("remapTestmodJar") {
    val testmodJarTasks = mutableListOf<Task>()

    subprojects {
        if (META_PROJECTS.contains(name) || !(file("src/testmod").exists() || file("src/testmodClient").exists())) {
            return@subprojects
        }

        testmodJarTasks.add(tasks.getByName("remapTestmodJar"))
    }

    nestedJars.setFrom(testmodJarTasks)
    addNestedDependencies = true
    clientOnlySourceSetName = sourceSets.getByName("testmodClient").name
}

loom {
    /*
 // Required as the item-group API uses access widened classes in its API, without this the javadoc generation fails.
 accessWidenerPath = file("notebook-item-group-api-v1/src/main/resources/notebook-item-group-api-v1.accesswidener")
 */
}

tasks.withType<Javadoc>() {
    options {
        source("21")
        encoding = "UTF-8"
        charset("UTF-8")
        memberLevel = JavadocMemberLevel.PACKAGE
        (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    allprojects.forEach {
        if (META_PROJECTS.contains(it.name)) {
            return@forEach
        }

        source(it.sourceSets.getByName("main").allJava)
        source(it.sourceSets.getByName("client").allJava)
    }

    classpath = files(sourceSets.getByName("main").compileClasspath, sourceSets.getByName("client").compileClasspath)
    include("**/api/**")
    isFailOnError = true
}

tasks.register("javadocJar", Jar::class) {
    dependsOn(tasks.javadoc)
    from(tasks.javadoc.get().destinationDir)

    archiveClassifier.set("fatjavadoc")
}

tasks.build.get().dependsOn(tasks.getByName("javadocJar"))

loom {
    runs {
        create("gametest") {
            inherit(getByName("testmodServer"))

            name = "Game Test"

            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${project.buildDir}/junit.xml")
            runDir("build/gametest")
        }
        create("autoTestServer") {
            inherit(getByName("testmodServer"))
            name = "Auto Test Server"
            vmArg("-Dfabric.autoTest")
        }
        create("autoTestClient") {
            inherit(getByName("testmodClient"))
            name = "Auto Test Client"
            vmArg("-Dfabric.autoTest")
            vmArg("-Dfabric-tag-conventions-v2.missingTagTranslationWarning=fail")
            vmArg("-Dfabric-tag-conventions-v1.legacyTagWarning=fail")
        }

        create("gametestCoverage") {
            inherit(getByName("gametest"))
            name = "Game Test Coverage"
            isIdeConfigGenerated = false
        }

        create("autoTestClientCoverage") {
            inherit(getByName("autoTestClient"))
            name = "Auto Test Client Coverage"
            isIdeConfigGenerated = false
        }
    }
}
tasks.test.get().dependsOn(tasks.getByName("runGametest"))

spotless {
    kotlinGradle {
        target("src/**/*.gradle", "*.gradle", "gradle/*.gradle")
        eclipse
    }
}

fun addPomMetadataInformation(project: Project, pom: MavenPom) {
    var modJsonFile = project.file("src/main/resources/notebook.mod.json")

    if (!modJsonFile.exists()) {
        modJsonFile = project.file("src/client/resources/notebook.mod.json")
    }

    pom.licenses {
        license {
            name = "MIT"
            url = "https://github.com/Bookkeepersmc/notebook/blob/HEAD/LICENSE"
        }
    }
    pom.developers {
        developer {
            name = "BookkeepersMC"
            url = "https://bookkeepersmc.github.io/"
        }
    }
    pom.scm {
        connection = "scm:git:https://github.com/Bookkeepersmc/notebook.git"
        url = "https://github.com/Bookkeepersmc/notebook"
        developerConnection = "scm:git:git@github.com:Bookkeepersmc/notebook.git"
    }
    pom.issueManagement {
        system = "GitHub"
        url = "https://github.com/Bookkeepersmc/notebook/issues"
    }
}

subprojects {
    if (META_PROJECTS.contains(name)) {
        return@subprojects
    }

    base {
        archivesName = project.name
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                pom {
                    addPomMetadataInformation(project, pom)
                }
                artifact(tasks.remapJar)

                artifact(tasks.remapSourcesJar) {
                    builtBy(tasks.remapSourcesJar)
                }
            }
        }
    }

    loom.disableDeprecatedPomGeneration(publishing.publications.getByName("mavenJava") as MavenPublication?)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.remapJar)

            artifact(tasks.getByName("sourcesJar")) {
                builtBy(tasks.remapSourcesJar)
            }

            artifact(tasks.getByName("javadocJar"))
            artifact(tasks.getByName("remapTestmodJar"))

            pom {
                addPomMetadataInformation(rootProject, pom)
            }
            pom.withXml {
                val depsNode = asNode().appendNode("dependencies")
                subprojects.forEach {
                    // The maven BOM containing all of the deprecated modules is added manually below.
                    if (it.path.startsWith(":deprecated") || META_PROJECTS.contains(it.name)) {
                        return@forEach
                    }

                    val depNode = depsNode.appendNode("dependency")
                    depNode.appendNode("groupId", it.group)
                    depNode.appendNode("artifactId", it.name)
                    depNode.appendNode("version", it.version)
                    depNode.appendNode("scope", "compile")
                }

                // Depend on the deprecated BOM to allow opting out of deprecated modules.
                val depNode = depsNode.appendNode("dependency")
                depNode.appendNode("groupId", group)
                depNode.appendNode("artifactId", "notebook-api-deprecated")
                depNode.appendNode("version", version)
                depNode.appendNode("scope", "compile")
            }
        }
    }
}

loom.disableDeprecatedPomGeneration(publishing.publications.getByName("mavenJava") as MavenPublication?)

fun setupRepositories(repositories: RepositoryHandler) {
    val env = System.getenv()
    if (env["MAVEN_URL"] != null) {
        repositories.maven {
            url = uri(env["MAVEN_URL"] as Any)
        }
    }
}

subprojects.forEach {
    if (META_PROJECTS.contains(it.name)) {
        return@forEach
    }

    tasks.remapJar.get().dependsOn("${it.path}:remapJar")
}

val devOnlyModules = listOf("notebook-gametest-api-v1")

dependencies {
    afterEvaluate {
        subprojects.forEach {
            if (META_PROJECTS.contains(it.name)) {
                return@forEach
            }

            api(project(path = "${it.path}", configuration = "namedElements"))
            "clientImplementation"(project("${it.path}:").sourceSets.getByName("client").output)

            "testmodImplementation"(project("${it.path}:").sourceSets.getByName("testmod").output)
            "testmodClientImplementation"(project("${it.path}:").sourceSets.getByName("testmodClient").output)
        }
    }
}

tasks.withType<net.fabricmc.loom.task.RemapJarTask>() {
    afterEvaluate {
        subprojects.forEach {
            if (it.name in devOnlyModules || META_PROJECTS.contains(it.name)) {
                return@forEach
            }

            // Include the signed or none signed jar from the sub project.
            nestedJars.from(project("${it.path}").tasks.getByName("remapJar"))
        }
    }
}


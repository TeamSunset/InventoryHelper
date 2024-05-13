import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("maven-publish")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.9.24"
    kotlin("kapt") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
}
val modId: String by project
val modName: String by project
val modVersion: String by project
val mavenGroup: String by project
val minecraftVersion: String by project
val loaderVersion: String by project
val yarnMappings: String by project
val fabricVersion: String by project

val targetJavaVersion = 17

val shade: Configuration by configurations.creating
val fullShade: Configuration by configurations.creating

version = modVersion
group = mavenGroup

base.archivesName.set(modId)

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    val mc = "com.mojang:minecraft:${minecraftVersion}"
    val yarn = "net.fabricmc:yarn:${yarnMappings}:v2"
    val fabricLoader = "net.fabricmc:fabric-loader:${loaderVersion}"
    val fabricApi = "net.fabricmc.fabric-api:fabric-api:${fabricVersion}"
    val fabricLanguageKotlin = "net.fabricmc:fabric-language-kotlin:1.10.20+kotlin.1.9.24"
    val jable = "com.github.dsx137:jable:1.0.10"

    minecraft(mc)
    mappings(yarn)
    modImplementation(fabricLoader)
    modImplementation(fabricApi)

    modImplementation(fabricLanguageKotlin)

    implementation(jable)
    shade(jable)
}

val props = mapOf(
    "mod_id" to modId,
    "mod_name" to modName,
    "mod_version" to modVersion,
    "minecraft_version" to minecraftVersion,
    "loader_version" to loaderVersion
)

tasks.processResources {
    val targets = listOf("fabric.mod.json")

    inputs.properties(props)

    filesMatching(targets) {
        expand(props)
    }
}

val generateTemplates by tasks.registering(Copy::class) {
    val src = file("src/main/templates/java")
    val dst = layout.buildDirectory.dir("generated/sources/templates/java")
    inputs.properties(props)
    outputs.upToDateWhen { false }

    from(src)
    into(dst)
    expand(props)
}
sourceSets["main"].java.srcDirs(generateTemplates.map { it.destinationDir })
rootProject.idea.project.settings.taskTriggers.afterSync(generateTemplates)
project.eclipse.synchronizationTasks(generateTemplates)

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        java.toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${modName}" }
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    minimize()
    minimize {
        fullShade.dependencies.forEach {
            exclude(dependency(it))
        }
    }

    configurations = listOf(shade, fullShade)

    relocate("com.github", "${mavenGroup}.inventoryhelper.shadowed.com.github")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = modId
            groupId = mavenGroup
            version = modVersion
            pom {
                name.set(modId)
            }
        }
    }
}

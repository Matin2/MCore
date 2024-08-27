import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("io.github.goooler.shadow") version "8.1.8"
    id("maven-publish")
    idea
}

group = "com.github.Matin2"
version = "1.3.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    flatDir { dirs("libs") }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.4.2")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.github.TheSilentPro:HeadDB:5.0.0-rc.11")
    implementation(fileTree("libs"))
    implementation("de.tr7zw:item-nbt-api:2.13.2")
    implementation("dev.jorel:commandapi-bukkit-shade:9.5.3")
    implementation("com.github.retrooper:packetevents-spigot:2.3.1-SNAPSHOT")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

tasks.shadowJar {
    val relocations = mapOf(
        "me.matin.mlib" to "mlib",
        "de.tr7zw.changeme.nbtapi" to "nbtapi",
        "dev.jorel.commandapi" to "commandapi",
        "assets" to "packetevents.assets",
        "com.github.retrooper.packetevents" to "packetevents.api",
        "io.github.retrooper.packetevents" to "packetevents.impl",
        "kotlinx" to "kotlinx",
    )
    relocate("me.matin.core.libs", relocations)
    dependencies {
        val depends = setOf(
            "org.jetbrains:annotations",
            "com.google.code.gson:gson",
            "net.kyori:",
            "org.slf4j:",
        )
        excludeDependencies(depends)
    }
    archiveFileName.set("${project.name}-${project.version}.jar")
}

private fun ShadowJar.relocate(dir: String, relocations: Map<String, String>) = relocations.forEach {
    relocate(it.key, "$dir.${it.value}")
}

private fun DependencyFilter.excludeDependencies(dependencies: Set<String>) = dependencies.forEach {
    exclude(dependency(it))
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.create<Copy>("copyJarToServer") {
    copy {
        from(layout.buildDirectory.dir("libs").get().asFile.path)
        rename("${project.name}-${project.version}.jar", "${project.name}.jar")
        into("F:/Minecraft/MCServer/planned/test/plugins")
    }
}
val javaVersion = 21

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.processResources {
    val ver = mapOf("version" to version)
    inputs.properties(ver)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(ver)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

kotlin {
    jvmToolchain(javaVersion)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
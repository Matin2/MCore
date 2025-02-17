import org.jetbrains.kotlin.gradle.internal.config.LanguageFeature

plugins {
    val kotlinVersion = "2.1.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("io.github.goooler.shadow") version "8.1.8"
    id("maven-publish")
    idea
}

group = "com.github.Matin2"
version = "1.3.0-SNAPSHOT 1"

repositories {
    mavenCentral()
    gradlePluginPortal()
    flatDir { dirs("libs") }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.5.2")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.github.TheSilentPro:HeadDB:5.0.0-rc.11")
    implementation(fileTree("libs"))
    implementation("de.tr7zw:item-nbt-api:2.14.1")
    implementation("dev.jorel:commandapi-bukkit-shade:9.7.0")
    implementation("com.github.retrooper:packetevents-spigot:2.7.0")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
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
    val exclusions = setOf(
        "org.jetbrains:annotations",
        "com.google.code.gson:gson",
        "net.kyori:",
        "org.slf4j:",
    )
    relocations.forEach { relocate(it.key, "me.matin.mcore.libs.${it.value}") }
    dependencies { exclusions.forEach { exclude(dependency(it)) } }
    archiveFileName.set("${project.name}-${project.version}.jar")
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

tasks.processResources {
    val ver = "version" to version.toString().replaceAfter('-', "SNAPSHOT")
    inputs.properties(ver)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(ver) }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

kotlin {
    jvmToolchain(javaVersion)
    val features = setOf(
        LanguageFeature.ExplicitBackingFields,
        LanguageFeature.WhenGuards,
        LanguageFeature.BreakContinueInInlineLambdas
    )
    compilerOptions {
        sourceSets.all { features.forEach { languageSettings.enableLanguageFeature(it.name) } }
    }
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
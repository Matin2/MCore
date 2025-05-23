import org.jetbrains.kotlin.gradle.internal.config.LanguageFeature

plugins {
    val kotlinVersion = "2.1.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("io.github.goooler.shadow") version "8.1.8"
    id("maven-publish")
    idea
}

group = "com.github.Matin2"
version = "1.2.9"

repositories {
    mavenCentral()
    gradlePluginPortal()
    flatDir { dirs("libs") }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.6.2")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.github.TheSilentPro:HeadDB:5.0.0-rc.11")
    implementation(fileTree("libs"))
    implementation("de.tr7zw:item-nbt-api:2.15.0")
    implementation("dev.jorel:commandapi-bukkit-shade:10.0.0")
    implementation("dev.jorel:commandapi-bukkit-kotlin:10.0.0")
    implementation("com.github.retrooper:packetevents-spigot:2.7.0")
    
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
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
        "org.jetbrains.kotlin:kotlin-stdlib",
        "org.jetbrains.kotlin:kotlin-reflect",
        "org.jetbrains.kotlinx:kotlinx-serialization-json",
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
val kotlinVersion = "2.1.10"
val javaVersion = 21

tasks.processResources {
    val ver = "version" to version.toString().replaceAfter('-', "SNAPSHOT")
    val kotlinVer = "kotlin_version" to kotlinVersion
    inputs.properties(ver, kotlinVer)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(ver, kotlinVer) }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

kotlin {
    jvmToolchain(javaVersion)
    val features = setOf(
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
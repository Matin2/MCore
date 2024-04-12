plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "com.github.Matin2"
description = "MCore"
version = "1.2.5"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/service/local/staging/deploy/maven2")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.github.retrooper.packetevents:spigot:2.2.1")

    api(kotlin("stdlib"))
    api(kotlin("reflect"))
}

tasks.shadowJar {
    relocate("co.aikar", "me.matin.core.aikar")
    relocate("assets", "me.matin.core.packetevents.assets")
    relocate("com.github.retrooper.packetevents", "me.matin.core.packetevents.api")
    relocate("io.github.retrooper.packetevents", "me.matin.core.packetevents.impl")
    relocate("org.intellij.lang.annotations", "me.matin.core.annotations.intellij")
    relocate("org.jetbrains.annotations", "me.matin.annotations.jetbrains")
    dependencies {
        exclude(dependency("com.google.code.gson:gson"))
        exclude(dependency("net.kyori:"))
    }
    archiveFileName.set("${project.name}-${project.version}.jar")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
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
    options.release.set(17)
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.description.toString()
            version = project.version.toString()

            from(components["java"])
        }
    }
}
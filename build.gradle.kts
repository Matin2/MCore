plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "com.github.Matin2"
description = "MCore"
version = "1.2.3"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://oss.sonatype.org/service/local/staging/deploy/maven2")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("com.mojang:brigadier"))
    }
    relocate("co.aikar.commands", "me.matin.core.aikar.commands")
    relocate("co.aikar.locales", "me.matin.core.aikar.locales")
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
plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

group = "com.github.Matin2"
description = "MCore"
version = "3.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://oss.sonatype.org/service/local/staging/deploy/maven2")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("net.axay:spigot-language-kotlin:1.0.3")
    compileOnly("net.kyori:adventure-api:4.16.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.16.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.2")
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
plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("io.github.goooler.shadow") version "8.1.7"
    id("maven-publish")
}

group = "com.github.Matin2"
version = "1.2.8"

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
    implementation("de.tr7zw:item-nbt-api:2.13.1")
    implementation("dev.jorel:commandapi-bukkit-shade:9.5.1")
    implementation("com.github.retrooper:packetevents-spigot:2.3.1-SNAPSHOT")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

tasks.shadowJar {
    val dir = "me.matin.core.libs"
    relocate("de.tr7zw.changeme.nbtapi", "$dir.nbtapi")
    relocate("dev.jorel.commandapi", "$dir.commandapi")
    relocate("assets", "$dir.packetevents.assets")
    relocate("com.github.retrooper.packetevents", "$dir.packetevents.api")
    relocate("io.github.retrooper.packetevents", "$dir.packetevents.impl")
    relocate("kotlinx", "$dir.kotlinx")
    relocate("_COROUTINE", "$dir.coroutine")
    dependencies {
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("com.google.code.gson:gson"))
        exclude(dependency("net.kyori:"))
        exclude(dependency("org.slf4j:"))
    }
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
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
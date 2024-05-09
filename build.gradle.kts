plugins {
    kotlin("jvm") version "1.9.24"
    id("io.github.goooler.shadow") version "8.1.7"
    id("maven-publish")
}

group = "com.github.Matin2"
version = "1.2.7"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    implementation("de.tr7zw:item-nbt-api:2.12.4")
    implementation("dev.jorel:commandapi-bukkit-shade:9.4.0")
    implementation("com.github.retrooper.packetevents:spigot:2.3.0")

    api(kotlin("stdlib"))
    api(kotlin("reflect"))
}

tasks.shadowJar {
    val dir = "me.matin.core.libs"
    relocate("de.tr7zw.changeme.nbtapi", "$dir.nbtapi")
    relocate("dev.jorel.commandapi", "$dir.commandapi")
    relocate("assets", "$dir.packetevents.assets")
    relocate("com.github.retrooper.packetevents", "$dir.packetevents.api")
    relocate("io.github.retrooper.packetevents", "$dir.packetevents.impl")
    dependencies {
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("com.google.code.gson:gson"))
        exclude(dependency("net.kyori:"))
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
plugins {
    kotlin("jvm") version "1.9.23"
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.paperweight.userdev") version "1.7.0"
    id("maven-publish")
}

group = "com.github.Matin2"
version = "1.2.6"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    implementation("de.tr7zw:item-nbt-api:2.12.4")
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:9.4.0")
    implementation("com.github.retrooper.packetevents:spigot:2.2.1")

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
}

tasks.register<Copy>("renameJar") {
    dependsOn("jar")
    dependsOn("shadowJar")
    from(layout.buildDirectory.dir("libs"))
    into(layout.buildDirectory.dir("libs"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    rename("(.+)-dev-all.jar", "$1.jar")
}

tasks.withType<GenerateModuleMetadata> {
    dependsOn("renameJar")
}

tasks.register<Delete>("deleteJars") {
    dependsOn("renameJar")
    delete(fileTree(layout.buildDirectory.dir("libs")).matching {
        include("**-dev**.jar")
    })
}

tasks.build {
    dependsOn(tasks.shadowJar)
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    finalizedBy("deleteJars")
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
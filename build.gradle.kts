import org.jetbrains.kotlin.gradle.internal.config.LanguageFeature

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.shadow)
	`version-catalog`
	`maven-publish`
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
	compileOnly(libs.paper)
	compileOnly(libs.skinsrestorer)
	
	api(fileTree("libs"))
	api(libs.nbtapi)
	api(libs.packetevents)
	
	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	compileOnly(libs.bundles.kotlinx)
}
val javaVersion = 21
tasks.compileJava {
	options.encoding = "UTF-8"
	options.release.set(javaVersion)
}

tasks.jar { manifest { attributes["paperweight-mappings-namespace"] = "mojang" } }

tasks.shadowJar {
	archiveClassifier = null
	manifest.inheritFrom(tasks.jar.get().manifest)
	val relocations = mapOf(
		"me.matin.mlib" to "mlib",
		"de.tr7zw.changeme.nbtapi" to "nbtapi",
		//PacketEvents
		"assets" to "packetevents.assets",
		"com.github.retrooper.packetevents" to "packetevents.api",
		"io.github.retrooper.packetevents" to "packetevents.impl",
	)
	dependencies { exclude { it.moduleGroup == "net.kyori" } }
	relocations.forEach { relocate(it.key, "me.matin.mcore.libs.${it.value}") }
}

tasks.build { dependsOn(tasks.shadowJar) }

tasks.processResources {
	mapOf(
		"version" to version.toString().replaceAfter('-', "SNAPSHOT"),
		"kotlin" to libs.versions.kotlin.get(),
		"coroutines" to libs.versions.kotlinx.coroutines.get(),
		"serialization" to libs.versions.kotlinx.serialization.get()
	).let {
		inputs.properties(it)
		filteringCharset = "UTF-8"
		filesMatching("plugin.yml") { expand(it) }
	}
}

kotlin {
	jvmToolchain(javaVersion)
	val features = setOf(
		LanguageFeature.ContextSensitiveResolutionUsingExpectedType,
		LanguageFeature.ExplicitBackingFields,
		LanguageFeature.ContextParameters
	)
	compilerOptions {
		sourceSets.all { features.forEach { languageSettings.enableLanguageFeature(it.name) } }
	}
}

publishing.publications.create<MavenPublication>("maven") {
	groupId = project.group.toString()
	artifactId = project.name
	version = project.version.toString()
	
	from(components["java"])
}
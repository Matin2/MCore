import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.jetbrains.kotlin.gradle.internal.config.LanguageFeature

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.shadow)
	alias(libs.plugins.paperweight)
	`version-catalog`
	`maven-publish`
	idea
}

group = "me.matin"
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
	paperweight.paperDevBundle(libs.versions.paper.get())
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

tasks.jar { enabled = false }

tasks.shadowJar {
	archiveClassifier = ""
	archiveVersion = ""
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

paperweight {
	reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

publishing {
	publications {
		create<MavenPublication>("shadow") {
			from(components["shadow"])
			
			groupId = "com.github.Matin2"
			artifactId = project.name
			version = project.version.toString()
		}
	}
}
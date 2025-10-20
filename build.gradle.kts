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
	compileOnly(libs.bundles.orm)
//	implementation("org.spongepowered:configurate-yaml:4.2.0")
//	implementation("org.spongepowered:configurate-extra-kotlin:4.2.0")
//	implementation("com.github.ItsDoot:configurate-serialization:0.1.1")
}
val javaVersion = 21
tasks {
	compileJava {
		options.encoding = "UTF-8"
		options.release.set(javaVersion)
	}
	
	jar { manifest { attributes["paperweight-mappings-namespace"] = "mojang" } }
	
	shadowJar {
		archiveClassifier = null
		manifest.inheritFrom(jar.get().manifest)
		val relocations = mapOf(
			"me.matin.mlib" to "mlib",
			"de.tr7zw.changeme.nbtapi" to "nbtapi",
			//PacketEvents
			"assets" to "packetevents.assets",
			"com.github.retrooper.packetevents" to "packetevents.api",
			"io.github.retrooper.packetevents" to "packetevents.impl",
			//Configurate
//			"org.spongepowered.configurate" to "configurate",
//			"io.leangen.geantyref" to "configurate.geantyref",
//			"pw.dotdash.configurate" to "configurate",
		)
		dependencies { exclude { it.moduleGroup == "net.kyori" } }
		relocations.forEach { relocate(it.key, "me.matin.mcore.libs.${it.value}") }
	}
	
	build { dependsOn(shadowJar) }
	
	processResources {
		val versions = mapOf(
			"version" to version.toString().replaceAfter('-', "SNAPSHOT"),
			"kotlin" to libs.versions.kotlin.get(),
			"coroutines" to libs.versions.kotlinx.coroutines.get(),
			"serialization" to libs.versions.kotlinx.serialization.get(),
			"exposed" to libs.versions.exposed.get(),
			"mysql" to libs.versions.mysql.get()
		)
		inputs.properties(versions)
		filteringCharset = "UTF-8"
		filesMatching("plugin.yml") { expand(versions) }
	}
}

kotlin {
	jvmToolchain(javaVersion)
	val features = setOf(
		LanguageFeature.ExplicitBackingFields,
		LanguageFeature.ContextParameters,
		LanguageFeature.NestedTypeAliases
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
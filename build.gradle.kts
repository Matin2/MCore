plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.shadow)
	alias(libs.plugins.paperweight)
	alias(libs.plugins.koin)
	`maven-publish`
}

group = "com.github.matin2"
version = "1.3.0-dev"

repositories {
	mavenCentral()
	gradlePluginPortal()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.codemc.io/repository/maven-public/")
	maven("https://jitpack.io")
}

dependencies {
	paperweight.paperDevBundle(libs.versions.paper.get())
	compileOnly(libs.skinsrestorer)
	
	api(libs.nbtapi)
	api(libs.packetevents)
	api(libs.koin)
	
	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	compileOnly(libs.bundles.kotlinx)
}

tasks.shadowJar {
	archiveClassifier = ""
	val relocations = mapOf(
		"me.matin.mlib" to "mlib",
		"de.tr7zw.changeme.nbtapi" to "nbtapi",
		//PacketEvents
		"assets" to "packetevents.assets",
		"com.github.retrooper.packetevents" to "packetevents.api",
		"io.github.retrooper.packetevents" to "packetevents.impl",
		//Koin
		"io.koin" to "koin",
	)
	dependencies { exclude { it.moduleGroup == "net.kyori" } }
	relocations.forEach { relocate(it.key, "me.matin.mcore.libs.${it.value}") }
}

tasks.build { dependsOn(tasks.shadowJar) }

tasks.processResources {
	mapOf(
		"version" to version.toString(),
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
	jvmToolchain(libs.versions.java.get().toInt())
	compilerOptions.freeCompilerArgs.addAll(
		"-Xexplicit-type-arguments",
		"-Xcollection-literals",
		"-Xcontext-sensitive-resolution",
		"-Xname-based-destructuring=complete"
	)
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

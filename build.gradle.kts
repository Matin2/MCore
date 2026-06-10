plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.shadow)
	alias(libs.plugins.paperweight)
	`version-catalog`
	`maven-publish`
}

group = "me.matin"
version = "1.2.9"

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
	
	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	compileOnly(libs.bundles.kotlinx)
}

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

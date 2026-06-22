plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.shadow)
	alias(libs.plugins.paperweight)
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
	compileOnly(libs.packetevents)
	
	api(libs.nbtapi)
	api(libs.koin)
	
	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	compileOnly(libs.bundles.kotlinx)
}

tasks.shadowJar {
	archiveClassifier = ""
	val relocations = mapOf(
		"de.tr7zw.changeme.nbtapi" to "nbtapi",
		//Koin
		"org.koin" to "koin",
		"co.touchlab" to "koin.touchlab",
	)
	dependencies { exclude { it.moduleGroup in listOf("org.jetbrains", "org.jetbrains.kotlin") } }
	relocations.forEach { relocate(it.key, "com.github.matin2.libs.${it.value}") }
}

tasks.jar { enabled = false }

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
		"-XXLanguage:+CompanionBlocksAndExtensions",
		"-Xexplicit-type-arguments",
		"-Xcollection-literals",
		"-Xcontext-sensitive-resolution",
		"-Xname-based-destructuring=complete"
	)
}

publishing.publications.create<MavenPublication>("maven") {
	from(components["shadow"])
	groupId = "com.github.Matin2"
}

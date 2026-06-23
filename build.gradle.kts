plugins {
	java
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
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
	api(kotlin("stdlib"))
	api(kotlin("reflect"))
	api(libs.bundles.kotlinx)
	api(libs.koin)
}

tasks.processResources {
	val version = "version" to version.toString()
	inputs.properties(version)
	filteringCharset = "UTF-8"
	filesMatching("paper-plugin.yml") { expand(version) }
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(25))
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

publishing.publications.create<MavenPublication>("maven") {
	from(components["java"])
	groupId = "com.github.Matin2"
}

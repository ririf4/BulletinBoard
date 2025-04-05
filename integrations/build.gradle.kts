plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
}

allprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

	dependencies {
		implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:+")
		implementation("com.h2database:h2:+")
		implementation("net.ririfa:langman:+")
	}
}

tasks.register("buildAllPlatform") {
	val buildDir = file("$rootDir/artifacts")
	buildDir.mkdirs()

	val (rel, maj, min) = rootProject.version.toString().split(".")
	println("Version: $rel.$maj.$min")

	val platformDirs = listOf("paper", "bungee", "velocity")

	val zipTasks = platformDirs.map { platform ->
		val platformName = "${rootProject.name}-$platform-$rel.$maj.$min"
		val taskName = "zip${platform.replaceFirstChar { it.uppercase() }}Jar"

		tasks.register<Zip>(taskName) {
			group = "build"
			description = "Zips the $platform JAR and runtime dependencies"
			destinationDirectory.set(buildDir)
			archiveFileName.set("$platformName.zip")

			from("$platform/build/libs") {
				include("*.jar")
			}

			from(configurations.runtimeClasspath.get().filterNot {
				it.name.startsWith("kotlin") || it.name.startsWith("slf4j")
			}.map { zipTree(it) })
		}
	}

	dependsOn("paper:jar", "bungee:jar", "velocity:jar")
	dependsOn(zipTasks)
}
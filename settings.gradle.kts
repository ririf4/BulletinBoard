pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven("https://repo.papermc.io/repository/maven-public/") { name = "PaperMC" }
		maven("https://repo.swiftstorm.dev/maven2/") { name = "SwiftStorm Repository" }
	}
}

rootProject.name = "BulletinBoard"

fun safeInclude(name: String, path: String) {
	val dir = file(path)
	if (dir.exists()) {
		include(name)
		project(":$name").projectDir = dir
	}
}

safeInclude("integrations:paper", "integrations/paper")
safeInclude("integrations:velocity", "integrations/velocity")
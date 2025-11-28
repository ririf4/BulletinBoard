pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven("https://repo.papermc.io/repository/maven-public/") { name = "PaperMC" }
		maven("https://repo.swiftstorm.dev/maven2/") { name = "SwiftStorm Repository" }
	}
}

rootProject.name = "BulletinBoard"

include("integrations:paper")
project(":integrations:paper").name = "${rootProject.name}-paper"

include("integrations:velocity")
project(":integrations:velocity").name = "${rootProject.name}-velocity"
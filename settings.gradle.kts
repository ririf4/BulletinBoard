pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
	}
	plugins {
		val kotlinVersion: String by settings

		kotlin("jvm") version kotlinVersion
		kotlin("plugin.serialization") version kotlinVersion
	}
}

rootProject.name = "BulletinBoard"

include("integrations")

include("integrations:paper")
include("integrations:velocity")
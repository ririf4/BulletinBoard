plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
}

group = "net.ririfa"
version = "1.0.0"

repositories {
	mavenCentral()
}

subprojects {
	repositories {
		mavenCentral()
		mavenLocal()
		maven("https://repo.papermc.io/repository/maven-public/")
		maven("https://repo.md-5.net/content/groups/public/")
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            name = "RiriFa"
            url = uri("https://repo.ririfa.net/maven2")
        }
        maven {
            name = "modrinth"
            url = uri("https://api.modrinth.com/maven")
        }
    }
}
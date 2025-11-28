plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.akkara.plugin) apply false

}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    group = "net.ririfa"
    version = when (name) {
        "${rootProject.name}-paper" -> "1.0.0"
        "${rootProject.name}-velocity" -> "1.0.0"
        else -> "0.0.0+DEV-${System.currentTimeMillis()}"
    }

	repositories {
		mavenCentral()
		mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/") { name = "PaperMC" }
        maven("https://repo.md-5.net/content/groups/public/") { name = "SpigotMC" }
        maven("https://oss.sonatype.org/content/repositories/snapshots") { name = "SonatypeOSS" }
        maven("https://repo.ririfa.net/maven2") { name = "RiriFaRepo" }
        maven("https://api.modrinth.com/maven") { name = "Modrinth" }
    }

    dependencies {
        implementation("net.ririfa:langman-core:+")
        implementation("net.ririfa:langman-ext.yaml:+")
        implementation("net.ririfa:beacon:+")
    }
}

subprojects {
    apply(plugin = "dev.swiftstorm.akkaradb-plugin")
}

tasks.register("buildAllPlatform") {
    dependsOn("paper:jar", "velocity:jar")

    doLast {
        val buildDir = "$rootDir/artifacts"
        println("Build directory: $buildDir")

        if (!file(buildDir).exists()) {
            file(buildDir).mkdir()
            println("Created build directory: $buildDir")
        }

        val (rel, maj, min) = rootProject.version.toString().split(".")
        println("Version: $rel.$maj.$min")

        val platformJars = mapOf(
            "paper" to "${rootProject.name}-paper-$rel.$maj.$min.jar",
            "velocity" to "${rootProject.name}-velocity-$rel.$maj.$min.jar"
        )

        platformJars.forEach { (platform, outputJarName) ->
            val platformDir = file("$platform/build/libs")
            println("Platform directory for $platform: $platformDir")

            val jarFiles = platformDir.listFiles { file -> file.extension == "jar" }
            if (jarFiles.isNullOrEmpty()) {
                println("No JAR files found for platform $platform.")
                return@forEach
            }

            val outputJar = file("$buildDir/$outputJarName")
            println("Output JAR will be created at: $outputJar")

            ant.withGroovyBuilder {
                "jar"("destfile" to outputJar, "duplicate" to "preserve") {
                    jarFiles.forEach { jarFile ->
                        println("Merging JAR file: $jarFile")
                        "zipfileset"("src" to jarFile)
                    }

                    configurations.runtimeClasspath.get().filter { artifact ->
                        !artifact.name.startsWith("kotlin") && !artifact.name.startsWith("slf4j")
                    }.forEach { artifact ->
                        println("Merging artifact: ${artifact.name}")
                        "zipfileset"("src" to artifact)
                    }
                }
            }

            println("Merged $platform JAR to $outputJar")
        }
    }
}
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
	id("de.eldoria.plugin-yml.paper") version "0.7.1"
	id("xyz.jpenilla.run-paper") version "2.3.1"
	id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

dependencies {
	paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
	compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

	paperLibrary("net.ririfa:igf:1.5.5-20250411.124811-4")
	paperLibrary(kotlin("stdlib"))
	paperLibrary(kotlin("reflect"))
}

paper {
	main = "net.ririfa.bulletinboard.BulletinBoard"
	loader = "net.ririfa.bulletinboard.loader.FabricordPluginLoader"
	generateLibrariesJson = true
	foliaSupported = false
	apiVersion = "1.21"
	version = rootProject.version.toString()
	name = rootProject.name
	load = BukkitPluginDescription.PluginLoadOrder.STARTUP
	authors = listOf("RiriFa", "cotrin_d8")
	description = "A simple bulletin board plugin"

	serverDependencies {
		register("LuckPerms") {
			required = false
			load = PaperPluginDescription.RelativeLoadOrder.BEFORE
		}
	}

	permissions {
		register("bulletinboard.*") {
			children = listOf(
				"bulletinboard.admin",
				"bulletinboard.gui.use",
				"bulletinboard.post.delete.own",
				"bulletinboard.post.create",
				"bulletinboard.post.edit.own",
				"bulletinboard.post.view",
				"bulletinboard.post.anonymous"
			)

			childrenMap = mapOf(
				"bulletinboard.admin" to true,
				"bulletinboard.gui.use" to true,
				"bulletinboard.post.delete.own" to true,
				"bulletinboard.post.create" to true,
				"bulletinboard.post.edit.own" to true,
				"bulletinboard.post.view" to true,
				"bulletinboard.post.anonymous" to true,
				"bulletinboard.post.delete.other" to true,
				"bulletinboard.post.edit.other" to true,
				"bulletinboard.reload" to true
			)
		}

		register("bulletinboard.admin") {
			description = "Allows the player to use all admin commands"
			default = BukkitPluginDescription.Permission.Default.OP
			children = listOf(
				"bulletinboard.post.delete.other",
				"bulletinboard.post.edit.other",
				"bulletinboard.reload"
			)
		}

		register("bulletinboard.gui.use") {
			description = "Allows the player to use the bulletinboard GUI"
			default = BukkitPluginDescription.Permission.Default.TRUE
		}

		register("bulletinboard.post.delete.own") {
			description = "Allows the player to delete posts"
			default = BukkitPluginDescription.Permission.Default.TRUE
		}

		register("bulletinboard.post.create") {
			description = "Allows the player to create posts"
			default = BukkitPluginDescription.Permission.Default.TRUE
		}

		register("bulletinboard.post.edit.own") {
			description = "Allows the player to edit their own posts"
			default = BukkitPluginDescription.Permission.Default.TRUE
		}

		register("bulletinboard.post.view") {
			description = "Allows the player to view posts"
			default = BukkitPluginDescription.Permission.Default.TRUE
		}

		register("bulletinboard.post.anonymous") {
			description = "Allows the player to post anonymously"
			default = BukkitPluginDescription.Permission.Default.TRUE
		}

		register("bulletinboard.post.delete.other") {
			description = "Allows the player to delete other players' posts"
			default = BukkitPluginDescription.Permission.Default.OP
		}

		register("bulletinboard.post.edit.other") {
			description = "Allows the player to edit other players' posts"
			default = BukkitPluginDescription.Permission.Default.OP
		}

		register("bulletinboard.post.debug") {
			description = "Allows the player to add a debug post"
			default = BukkitPluginDescription.Permission.Default.OP
		}

		register("bulletinboard.reload") {
			description = "Allows the player to reload the plugin"
			default = BukkitPluginDescription.Permission.Default.OP
		}
	}
}

tasks.named<Jar>("jar") {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(sourceSets.main.get().output)
//	from("LICENSE") {
//		rename { "${it}_${project.name}" }
//	}
	from({
		configurations.runtimeClasspath.get()
			.filter { file ->
				!file.name.startsWith("kotlin")
			}
			.map { file ->
				if (file.isDirectory) file else zipTree(file)
			}
	})
}
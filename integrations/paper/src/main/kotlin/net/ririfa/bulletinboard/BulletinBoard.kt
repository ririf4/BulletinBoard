@file:Suppress("DEPRECATION", "unused")

package net.ririfa.bulletinboard

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.ririfa.beacon.EventBus
import net.ririfa.bulletinboard.command.CommandManager
import net.ririfa.bulletinboard.gui.GUIRelListener
import net.ririfa.bulletinboard.translation.BBMSGProvider
import net.ririfa.bulletinboard.translation.BBMessageKey
import net.ririfa.bulletinboard.util.isOlderVersion
import net.ririfa.igf.IGF
import net.ririfa.langman.InitType
import net.ririfa.langman.LangMan
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.io.resolve
import kotlin.use

class BulletinBoard : JavaPlugin() {
	companion object {
		const val ID = "bulletinboard"

		lateinit var instance: BulletinBoard
			private set
		lateinit var dataBase: DataBase
			private set
		lateinit var langMan: LangMan<BBMSGProvider, TextComponent>
			private set
		val logger: Logger = LoggerFactory.getLogger(BulletinBoard::class.simpleName)
		val availableLang = listOf("en", "ja")
		val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
	}

	val version = description.version
	val authors: MutableList<String> = description.authors
	val pluginDes = description.description

	val langDir: Path = dataFolder.resolve("lang").toPath()

	override fun onLoad() {
		instance = this

		LanguageAutoUpdater.checkForUpdatesAndExtract()

		langMan = LangMan.createNew<BBMSGProvider, TextComponent>(
			{ Component.text(it) },
			BBMessageKey::class
		)

		langMan.init(InitType.YAML, langDir.toFile(), availableLang)
		EventBus.initialize("net.ririfa.bulletinboard")
		dataBase = DataBase(this)
		dataBase.start()
	}

	override fun onEnable() {
		IGF.init(this, "net.ririfa.bulletinboard")
		server.pluginManager.registerEvents(GUIRelListener(), this)
		registerCommand(this)
	}

	private val listener = object : Listener {
		@EventHandler
		fun onPlayerJoin(event: PlayerJoinEvent) {

		}

		@EventHandler
		fun onPlayerQuit(event: PlayerQuitEvent) {
			val gson = Gson()
			val player = event.player
			val playerInventory = player.inventory

			val nmsPlayer = (player as CraftPlayer).handle

		}
	}

	private fun registerCommand(plugin: JavaPlugin) {
		val commandMapField = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
		commandMapField.isAccessible = true
		val commandMap = commandMapField.get(Bukkit.getServer()) as CommandMap

		val command = object : Command("bulletinboard") {
			override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
				return CommandManager.onCommand(sender, this, label, args)
			}

			override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String?> {
				return CommandManager.onTabComplete(sender, this, alias, args)
			}
		}

		command.aliases = listOf("bb")
		command.description = "BulletinBoard Main Command"
		commandMap.register(plugin.description.name, command)
	}

	object LanguageAutoUpdater {
		private val yaml = Yaml()
		private const val DEFAULT_VERSION = "1.0.0"

		fun checkForUpdatesAndExtract() {
			try {
				if (!Files.exists(LangDir)) {
					Files.createDirectories(LangDir)
					extractLangFiles(LangDir)
					return
				}

				val latestVersions = getLatestVersionsFromJar() ?: return
				val needsUpdate = Files.list(LangDir).use { files ->
					files.toList().filter { it.toString().endsWith(".yml") }.any { file ->
						val langKey = file.fileName.toString().removeSuffix(".yml")
						val latestVersion = latestVersions[langKey] ?: DEFAULT_VERSION
						val currentVersion = getVersionFromYaml(file) ?: DEFAULT_VERSION
						isOlderVersion(currentVersion, latestVersion)
					}
				}

				if (needsUpdate) {
					extractLangFiles(LangDir)
				}
			} catch (e: Exception) {
				logger.error("Failed to check for language file updates", e)
			}
		}

		private fun extractLangFiles(targetDir: Path) {
			try {
				val langPath = "assets/${ID}/lang/"
				val classLoader = BulletinBoard::class.java.classLoader

				availableLang.forEach { lang ->
					val fileName = "$lang.yml"
					val fullPath = "$langPath$fileName"

					var inputStream: InputStream? = classLoader.getResourceAsStream(fullPath)

					if (inputStream == null) {
						val fallbackPath = Path.of("build/resources/main/$fullPath")
						if (Files.exists(fallbackPath)) {
							inputStream = Files.newInputStream(fallbackPath)
							logger.warn("Using fallback language file: $fallbackPath")
						}
					}

					if (inputStream == null) {
						logger.warn("Language file not found: $fullPath (also missing in build/resources/main)")
						return@forEach
					}

					val targetFile = targetDir.resolve(fileName)
					Files.copy(inputStream, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
					inputStream.close()
					logger.info("Extracted language file: $fileName")
				}
			} catch (e: Exception) {
				logger.error("Failed to extract language files", e)
			}
		}

		private fun getVersionFromYaml(file: Path): String? {
			return try {
				Files.newBufferedReader(file).use { reader ->
					val data = yaml.load<Map<String, Any>>(reader)
					data["version"] as? String
				}
			} catch (e: Exception) {
				logger.warn("Failed to read version from ${file.fileName}", e)
				null
			}
		}

		private fun getLatestVersionsFromJar(): Map<String, String>? {
			return try {
				val classLoader = this::class.java.classLoader
				val resourceUrl = classLoader.getResource("assets/${ID}/lang/langversion.info") ?: return null
				resourceUrl.openStream().use { inputStream ->
					val data: Map<String, Any> = yaml.load(inputStream)
					@Suppress("UNCHECKED_CAST")
					data["latest"] as? Map<String, String>
				}
			} catch (e: Exception) {
				logger.error("Failed to read langversion.info", e)
				null
			}
		}
	}
}
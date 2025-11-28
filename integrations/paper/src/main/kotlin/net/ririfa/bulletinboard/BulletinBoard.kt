@file:Suppress("DEPRECATION", "unused")

package net.ririfa.bulletinboard

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.ririfa.bulletinboard.command.CommandManager
import net.ririfa.bulletinboard.gui.GUIRelListener
import net.ririfa.bulletinboard.translation.BBMSGProvider
import net.ririfa.bulletinboard.translation.BBMessageKey
import net.ririfa.igf.IGF
import net.ririfa.langman.LangMan
import net.ririfa.langman.LangManBuilder
import net.ririfa.langman.TextFactory
import net.ririfa.langman.ext.yaml.YamlFileLoader
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
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class BulletinBoard : JavaPlugin() {

	companion object {
		const val ID = "bulletinboard"

		lateinit var instance: BulletinBoard
			private set

		lateinit var langMan: LangMan<BBMSGProvider, TextComponent>
			private set

		lateinit var dataBase: DataBase
			private set

		val logger: Logger = LoggerFactory.getLogger(BulletinBoard::class.simpleName)
		val availableLang: List<String> = listOf("en", "ja")
		val threads: Int = Runtime.getRuntime().availableProcessors()
		val thread: ScheduledExecutorService = Executors.newScheduledThreadPool(
			(threads * 0.3)
				.toInt()
				.coerceAtLeast(2)
				.coerceAtMost(8)
		) { r ->
			Thread(r, "BulletinBoard-Worker").apply {
				isDaemon = true
			}
		}
	}

	val version: String get() = description.version
	val authors: MutableList<String> get() = description.authors
	val pluginDes: String get() = description.description.orEmpty()

	val langDir: Path get() = dataFolder.resolve("lang").toPath()
	val dbDir: Path get() = dataFolder.resolve("database").toPath()

	override fun onLoad() {
		instance = this
		initLanguage()
	}

	override fun onEnable() {
		IGF.init(this, "net.ririfa.bulletinboard")
		server.pluginManager.registerEvents(GUIRelListener(), this)
		server.pluginManager.registerEvents(playerListener, this)
		registerMainCommand()
	}

	fun <T> execute(task: () -> T): T {
		val resultHolder = arrayOfNulls<Any>(1)
		val latch = CountDownLatch(1)

		Bukkit.getScheduler().runTask(Plugin, Runnable {
			try {
				resultHolder[0] = task()
			} finally {
				latch.countDown()
			}
		})

		latch.await()
		@Suppress("UNCHECKED_CAST")
		return resultHolder[0] as T
	}

	/**
	 * Language System initialization
	 */
	private fun initLanguage() {
		langMan = LangManBuilder.new<BBMSGProvider, TextComponent>()
			.fromResource("/assets/$ID/lang/")
			.toPath(langDir)
			.withMessageKey(BBMessageKey::class.java)
			.withType(YamlFileLoader { input -> Yaml().load(input) })
			.registerTextFactory(textFactory)
			.withLanguage(availableLang)
			.autoUpdateIfNeeded(true)
			.debug(true)
			.build()
	}

	/**
	 * Main command registration using CommandMap
	 */
	private fun registerMainCommand() {
		val commandMapField = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
		commandMapField.isAccessible = true
		val commandMap = commandMapField.get(Bukkit.getServer()) as CommandMap

		val command = object : Command("bulletinboard") {
			override fun execute(
				sender: CommandSender,
				label: String,
				args: Array<String>
			): Boolean = CommandManager.onCommand(sender, this, label, args)

			override fun tabComplete(
				sender: CommandSender,
				alias: String,
				args: Array<out String>
			): List<String?> = CommandManager.onTabComplete(sender, this, alias, args)
		}

		command.aliases = listOf("bb")
		command.description = "BulletinBoard Main Command"
		commandMap.register(description.name, command)
	}

	/**
	 * Player events
	 */
	private val playerListener = object : Listener {

		@EventHandler
		fun onPlayerJoin(event: PlayerJoinEvent) {
		}

		@EventHandler
		fun onPlayerQuit(event: PlayerQuitEvent) {
			val gson = Gson()
			val player = event.player
			val inv = player.inventory

			val nmsPlayer = (player as CraftPlayer).handle
		}
	}

	/**
	 * Text factory used by LangMan
	 */
	private val textFactory = object : TextFactory<TextComponent> {
		override val clazz: Class<TextComponent>
			get() = TextComponent::class.java

		override fun invoke(text: String): TextComponent =
			Component.text(text)
	}
}

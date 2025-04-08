@file:Suppress("DEPRECATION", "unused")

package net.ririfa.bulletinboard

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.ririfa.bulletinboard.command.CommandManager
import net.ririfa.bulletinboard.translation.BBMSGProvider
import net.ririfa.bulletinboard.translation.BBMessageKey
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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class BulletinBoard : JavaPlugin() {
	companion object {
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

	private val langDir = dataFolder.resolve("lang")

	override fun onLoad() {
		instance = this

		langMan = LangMan.createNew<BBMSGProvider, TextComponent>(
			{ Component.text(it) },
			BBMessageKey::class
		)

		langMan.init(InitType.YAML, langDir, availableLang)
		IGF.init(this, "net.ririfa.bulletinboard")
		dataBase = DataBase(this)
		dataBase.start()
	}

	override fun onEnable() {
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
}
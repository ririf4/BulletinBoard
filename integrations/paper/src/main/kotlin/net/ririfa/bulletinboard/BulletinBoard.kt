package net.ririfa.bulletinboard

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.ririfa.bulletinboard.translation.BBMSGProvider
import net.ririfa.bulletinboard.translation.BBMessageKey
import net.ririfa.langman.InitType
import net.ririfa.langman.LangMan
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BulletinBoard : JavaPlugin() {
	companion object {
		val logger: Logger = LoggerFactory.getLogger(BulletinBoard::class.simpleName)

		val availableLang = listOf("en", "ja")
	}

	private val langDir = dataFolder.resolve("lang")

	override fun onLoad() {
		val langMan = LangMan.createNew<BBMSGProvider, TextComponent>(
			{ Component.text(it) },
			BBMessageKey::class
		)

		langMan.init(InitType.YAML, langDir, availableLang)


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
}
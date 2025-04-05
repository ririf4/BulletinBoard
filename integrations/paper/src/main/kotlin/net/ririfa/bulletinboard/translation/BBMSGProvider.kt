package net.ririfa.bulletinboard.translation

import net.kyori.adventure.text.TextComponent
import net.ririfa.langman.def.MessageProviderDefault
import org.bukkit.entity.Player

class BBMSGProvider(private val player: Player) : MessageProviderDefault<BBMSGProvider, TextComponent>(
	TextComponent::class.java
) {
	override fun getLanguage(): String {
		return player.locale().language
	}
}

fun Player.adapt(): BBMSGProvider {
	return BBMSGProvider(this)
}
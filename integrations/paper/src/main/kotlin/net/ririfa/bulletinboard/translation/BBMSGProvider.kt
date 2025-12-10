package net.ririfa.bulletinboard.translation

import net.kyori.adventure.text.TextComponent
import net.ririfa.langman.MessageKey
import net.ririfa.langman.def.MessageProviderDefault
import org.bukkit.entity.Player

class BBMSGProvider(private val player: Player) : MessageProviderDefault<BBMSGProvider, TextComponent>(
	TextComponent::class.java,
	BBMessageKey::class.java
) {
	override fun getLanguage(): String {
		return player.locale().language
	}
}

fun Player.adapt(): BBMSGProvider {
	return BBMSGProvider(this)
}

fun Player.getMessage(key: BBMessageKey, arguments: Map<String, Any> = emptyMap()): TextComponent {
	val provider = adapt()
	return provider.getMessage(key, arguments)
}

fun MessageKey<BBMSGProvider, TextComponent>.t(player: Player): TextComponent {
	val provider = player.adapt()
	return provider.getMessage(this)
}
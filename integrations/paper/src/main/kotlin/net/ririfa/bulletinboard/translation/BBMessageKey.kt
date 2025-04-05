package net.ririfa.bulletinboard.translation

import net.kyori.adventure.text.TextComponent
import net.ririfa.langman.MessageKey

sealed interface BBMessageKey : MessageKey<BBMSGProvider, TextComponent> {

}
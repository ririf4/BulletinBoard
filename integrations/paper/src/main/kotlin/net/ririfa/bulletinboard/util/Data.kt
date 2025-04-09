package net.ririfa.bulletinboard.util

import net.kyori.adventure.text.TextComponent
import java.util.*

data class Post(
    val id: ShortUUID,
    val author: UUID,
    val title: TextComponent,
    val content: TextComponent,
    val isAnonymous: Boolean,
    val date: Date,
    val isDeleted: Boolean,
)
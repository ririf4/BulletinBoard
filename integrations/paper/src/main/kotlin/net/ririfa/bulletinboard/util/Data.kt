package net.ririfa.bulletinboard.util

import net.kyori.adventure.text.Component
import java.util.*

data class Post(
    val id: ShortUUID,
    val author: UUID,
    val title: Component,
    val content: Component,
    val isAnonymous: Boolean,
    val date: Date,
    val isDeleted: Boolean,
)
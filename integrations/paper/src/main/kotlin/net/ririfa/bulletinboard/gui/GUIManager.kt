package net.ririfa.bulletinboard.gui

import net.ririfa.bulletinboard.util.Post
import org.bukkit.entity.Player

interface GUIManager {
    fun openGUI(player: Player, posts: List<Post>)
}
package net.ririfa.bulletinboard.gui

import io.papermc.paper.event.player.AsyncChatEvent
import net.ririfa.bulletinboard.util.getPlayerState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GUIRelListener : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val state = player.getPlayerState()
        if (state.inputState.isInputting == false) return

        //TODO
    }
}
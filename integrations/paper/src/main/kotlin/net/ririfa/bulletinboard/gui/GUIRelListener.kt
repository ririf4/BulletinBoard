@file:Suppress("DuplicatedCode")

package net.ririfa.bulletinboard.gui

import io.papermc.paper.event.player.AsyncChatEvent
import net.ririfa.bulletinboard.translation.BBMessageKey
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.PlayerMode
import net.ririfa.bulletinboard.util.getPlayerState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GUIRelListener : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val state = player.getPlayerState()
        val message = event.message() // Adventure Component
        val p = player.adapt()

        when (state.mode) {

            PlayerMode.AWAITING_TITLE_INPUT -> {
                event.isCancelled = true
                val draft = state.draft
                if (draft == null) {
                    player.sendMessage(p.getMessage(BBMessageKey.Messages.PostDraftNull))
                    state.reset()
                    return
                }

                draft.title = message
                state.mode = PlayerMode.CREATING_POST
                GUIManager.openGUI(player, GUIState.NEW_POST)
            }

            PlayerMode.AWAITING_CONTENT_INPUT -> {
                event.isCancelled = true
                val draft = state.draft
                if (draft == null) {
                    player.sendMessage(p.getMessage(BBMessageKey.Messages.PostDraftNull))
                    state.reset()
                    return
                }

                draft.content = message
                state.mode = PlayerMode.CREATING_POST
                GUIManager.openGUI(player, GUIState.NEW_POST)
            }

            PlayerMode.AWAITING_EDIT_TITLE_INPUT -> {
                event.isCancelled = true
                val draft = state.draft
                if (draft == null) {
                    player.sendMessage(p.getMessage(BBMessageKey.Messages.PostDraftNull))
                    state.reset()
                    return
                }

                draft.title = message
                state.mode = PlayerMode.EDITING_POST
                GUIManager.openGUI(player, GUIState.EDIT_POST)
            }

            PlayerMode.AWAITING_EDIT_CONTENT_INPUT -> {
                event.isCancelled = true
                val draft = state.draft
                if (draft == null) {
                    player.sendMessage(p.getMessage(BBMessageKey.Messages.PostDraftNull))
                    state.reset()
                    return
                }

                draft.content = message
                state.mode = PlayerMode.EDITING_POST
                GUIManager.openGUI(player, GUIState.EDIT_POST)
            }

            else -> { /* Do nothing */
            }
        }
    }
}

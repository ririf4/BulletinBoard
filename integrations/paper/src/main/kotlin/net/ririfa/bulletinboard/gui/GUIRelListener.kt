package net.ririfa.bulletinboard.gui

import io.papermc.paper.event.player.AsyncChatEvent
import net.ririfa.bulletinboard.util.InputType
import net.ririfa.bulletinboard.util.getPlayerState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@Suppress("DuplicatedCode")
class GUIRelListener : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val state = player.getPlayerState()
        val message = event.message()

        if (state.inputState.isInputting == true) {
            event.isCancelled = true

            when (state.inputState.inputType) {
                InputType.TITLE -> state.draftState.draft?.title = message
                InputType.CONTENT -> state.draftState.draft?.content = message
                else -> state.inputState.isInputting = null
            }

            state.inputState.isInputting = null
            state.inputState.inputType = null
        } else if (state.inputState.isEditInputting == true) {
            event.isCancelled = true

            when (state.inputState.editInputType) {
                InputType.TITLE -> state.draftState.editDraft?.title = message
                InputType.CONTENT -> state.draftState.editDraft?.content = message
                else -> state.inputState.isEditInputting = null
            }

            state.inputState.isEditInputting = null
            state.inputState.editInputType = null
        }

        GUIManager.openGUI(player, GUIState.NEW_POST)
    }
}
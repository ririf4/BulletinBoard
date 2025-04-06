package net.ririfa.bulletinboard.gui

import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.igf.Button
import org.bukkit.entity.Player

object GUIManager {
    fun openGUI(player: Player) {
        val ap = player.adapt()

        val buttonMapping = mapOf<GUIState, List<Button>>(
            GUIState.MAIN_BOARD to listOf(

            ),
            GUIState.MY_POSTS to listOf(

            ),
            GUIState.POSTS to listOf(

            )
        )
    }
}
package net.ririfa.bulletinboard.command

import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.DataBase
import net.ririfa.bulletinboard.gui.GUIManager.openGUI
import net.ririfa.bulletinboard.gui.GUIState
import net.ririfa.bulletinboard.translation.BBMessageKey
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.*
import org.bukkit.entity.Player
import java.util.*

typealias CommandExecute = (Player, Array<String>) -> Unit

enum class Commands(val execute: CommandExecute) {
    OPENBOARD({ player, _ -> openGUI(player) }),
    NEWPOST({ player, _ -> openGUI(player, GUIState.NEW_POST) }),
    ALLPOSTS({ player, _ -> openGUI(player, GUIState.ALL_POSTS) }),
    MYPOSTS({ player, _ -> openGUI(player, GUIState.MY_POSTS) }),
    DELETEDPOSTS({ player, _ -> openGUI(player, GUIState.DELETED_POSTS) }),
    PREVIEWCLOSE({ player, _ ->
        val state = player.getPlayerState()
        val p = player.adapt()

        if (state.mode == PlayerMode.PREVIEWING_POST && state.draft != null) {
            when (state.previewBeforeMode) {
                PlayerMode.CREATING_POST -> {
                    openGUI(player, GUIState.NEW_POST)
                }

                PlayerMode.EDITING_POST -> {
                    openGUI(player, GUIState.EDIT_POST)
                }

                else -> {
                    state.reset()
                    player.sendMessage(p.getMessage(BBMessageKey.Messages.NotPreviewing))
                }
            }
        } else {
            player.sendMessage(p.getMessage(BBMessageKey.Messages.NotPreviewing))
        }
    }),
    HELP({ player, _ -> displayHelp(player) }),
    ABOUT({ player, _ -> displayAbout(player) }),

    INSERTDEBUGPOST({ player, args ->
        if (player.hasPermission("bulletinboard.post.debug")) {
            if (args.size > 1 && (args[1] == "0" || args[1] == "1")) {
                val isAnonymous = args[1] == "1"

                val post = DataBase.Post(
                    id = ShortUUID.generate(),
                    title = Component.text("Debug Post"),
                    content = Component.text("This is a debug post"),
                    author = player.uniqueId,
                    isAnonymous = isAnonymous,
                    date = Date(),
                    editedOn = Date(),
                    isDeleted = false
                )
                DataBase.insertPost(post)
                player.sendMessage("Debug post inserted with isAnonymous set to $isAnonymous")
            } else {
                player.sendMessage("Usage: /insertdebugpost <0 or 1>")
            }
        } else {
            player.sendMessage("You do not have permission to use this command.")
        }
    }),
    RELOAD({ player, _ ->
        if (player.hasPermission("bulletinboard.reload")) {
            //TODO
            //BulletinBoard.get()?.reload(player)
        } else {
            player.sendMessage("You do not have permission to use this command.")
        }
    });

    companion object {
        fun fromString(name: String): Commands? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}
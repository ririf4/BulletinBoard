package net.ririfa.bulletinboard.command

import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.DataBase
import net.ririfa.bulletinboard.gui.GUIManager.openGUI
import net.ririfa.bulletinboard.gui.GUIState
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
        //TODO
//        val state = player.getPlayerState()
//        val p = player.adapt()
//        if (state.preview != null) {
//            state.isPreviewing = null
//            state.preview = null
//            openPostEditor(player)
//        } else {
//            player.sendMessage(p.getMessage(Main.Command.Message.NOT_PREVIEWING))
//        }
    }),
    HELP({ player, _ -> displayHelp(player) }),
    ABOUT({ player, _ -> displayAbout(player) }),

    DEBUG({ player, _ -> player.getPlayerState().sendDebugMessage(player) }),
    INSERTDEBUGPOST({ player, args ->
        if (player.hasPermission("bulletinboard.post.debug")) {
            if (args.size > 1 && (args[1] == "0" || args[1] == "1")) {
                val isAnonymous = args[1] == "1"

                val post = Post(
                    id = ShortUUID.generate(),
                    title = Component.text("Debug Post"),
                    content = Component.text("This is a debug post"),
                    author = player.uniqueId,
                    isAnonymous = isAnonymous,
                    date = Date(),
                    isDeleted = false
                )
                DataBase.Accessor.insertPost(post)
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
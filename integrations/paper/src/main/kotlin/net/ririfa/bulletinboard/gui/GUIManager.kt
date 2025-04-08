package net.ririfa.bulletinboard.gui

import net.ririfa.bulletinboard.DataBase
import net.ririfa.bulletinboard.gui.GUIState.*
import net.ririfa.bulletinboard.translation.BBMessageKey.GUI
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.displayAbout
import net.ririfa.bulletinboard.util.displayHelp
import net.ririfa.bulletinboard.util.displayPost
import net.ririfa.igf.Button
import net.ririfa.igf.PaginatedDynamicGUI
import net.ririfa.igf.setClick
import net.ririfa.igf.setClickTyped
import org.bukkit.Material
import org.bukkit.entity.Player

object GUIManager {
    fun openGUI(player: Player, state: GUIState = MAIN_BOARD) {
        val ap = player.adapt()
        val middleRowSlots = listOf(10, 12, 14, 16)

        val buttonMapping = mapOf<GUIState, List<Button>>(
            // Dynamic
            MAIN_BOARD to listOf(
                Button(10, Material.WRITABLE_BOOK, GUI.Buttons.MainBoard.NewPost.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.switchState(NEW_POST)
                    },
                Button(12, Material.BOOK, GUI.Buttons.MainBoard.AllPosts.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.switchState(ALL_POSTS)
                    },
                Button(14, Material.WRITTEN_BOOK, GUI.Buttons.MainBoard.MyPosts.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.switchState(MY_POSTS)
                    },
                Button(16, Material.CAULDRON, GUI.Buttons.MainBoard.DeletedPosts.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.switchState(DELETED_POSTS)
                    },
                Button(29, Material.LECTERN, GUI.Buttons.MainBoard.AboutPlugin.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.close()
                        displayAbout(player)
                    },
                Button(31, Material.COMPARATOR, GUI.Buttons.MainBoard.Settings.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.switchState(SETTINGS)
                    },
                Button(33, Material.OAK_SIGN, GUI.Buttons.MainBoard.Help.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.close()
                        displayHelp(player)
                    },
            ),
            // Dynamic
            NEW_POST to listOf(

            ),
            // Page + Dynamic
            MY_POSTS to listOf(

            ),
            // Page + Dynamic
            ALL_POSTS to listOf(

            ),
            //TODO: ADD MORE GUIs !!! :D
        )

        val pageChangeButton = Pair(
            Button(
                18,
                Material.ARROW,
                ap.getMessage(GUI.Buttons.Paged.Previous)
            ),
            Button(
                26,
                Material.ARROW,
                ap.getMessage(GUI.Buttons.Paged.Next)
            )
        )

        val noPosts = Button(13, Material.PAPER, ap.getMessage(GUI.Other.NoPosts))

        val gui = PaginatedDynamicGUI.of<GUIState>(player)
            .setStateButtonMapping(buttonMapping)
            .setSlotPositions(middleRowSlots)
            .setPageItemProvider { state -> resolvePageButtons(state, player, middleRowSlots) }
            .setItemsPerPage(middleRowSlots.size)
            .setEmptyMessageButton(noPosts)
            .setPageChangeButtons(pageChangeButton.first, pageChangeButton.second)
            .setState(state)
            // End of the PaginatedGUI configuration
            .setSize(27)
            // PaginatedDynamicGUI isn't changing Inventory. so we can't change the title for each state
            .setTitle(ap.getMessage(GUI.Title))
            .setBackground(Material.GRAY_STAINED_GLASS_PANE)
            .build()

        gui.open()
    }

    private fun resolvePageButtons(
        state: GUIState,
        player: Player,
        middleRowSlots: List<Int>
    ): List<Button> {
        return when (state) {
            MAIN_BOARD, NEW_POST, SETTINGS -> emptyList()
            MY_POSTS -> {
                DataBase.Accessor.getMyPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClick { displayPost(player, post) }
                        }
                    }
            }

            ALL_POSTS -> {
                DataBase.Accessor.getAllPosts()
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClick { displayPost(player, post) }
                        }
                    }
            }

            DELETED_POSTS -> {
                DataBase.Accessor.getDeletedPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClick { displayPost(player, post) }
                        }
                    }
            }
        }
    }
}
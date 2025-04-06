package net.ririfa.bulletinboard.gui.impl

import net.ririfa.bulletinboard.gui.GUIManager
import net.ririfa.bulletinboard.gui.GUIState
import net.ririfa.bulletinboard.translation.BBMessageKey.GUI
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.Post
import net.ririfa.bulletinboard.util.displayPost
import net.ririfa.igf.Button
import net.ririfa.igf.PaginatedDynamicGUI
import net.ririfa.igf.setClick
import net.ririfa.igf.setClickTyped
import org.bukkit.Material
import org.bukkit.entity.Player

object GUIManagerImpl : GUIManager {
    override fun openGUI(player: Player, posts: List<Post>) {
        val ap = player.adapt()
        val middleRowSlots = listOf(10, 12, 14, 16)
        val postButtons = if (posts.isNotEmpty()) {
            posts.mapIndexed { index, post ->
                Button(middleRowSlots.getOrNull(index) ?: -1, Material.WRITTEN_BOOK, post.title)
                    .setClick { displayPost(player, post) }
            }
        } else {
            emptyList()
        }

        val buttonMapping = mapOf<GUIState, List<Button>>(
            // Dynamic
            GUIState.MAIN_BOARD to listOf(
//                Button(10, Material.WRITABLE_BOOK, Main.Gui.Button.NEW_POST.t(p), mainBoardKey, CustomID.NEW_POST.name),
                Button(12, Material.BOOK, GUI.Buttons.MainBoard.AllPosts.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.switchState(GUIState.ALL_POSTS)
                    },
                Button(14, Material.WRITTEN_BOOK, GUI.Buttons.MainBoard.MyPosts.t(ap))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                        gui.switchState(GUIState.MY_POSTS)
                    },
//                Button(16, Material.CAULDRON, Main.Gui.Button.DELETED_POSTS.t(p), mainBoardKey, CustomID.DELETED_POSTS.name),
//                Button(29, Material.LECTERN, Main.Gui.Button.ABOUT_PLUGIN.t(p), mainBoardKey, CustomID.ABOUT_PLUGIN.name),
//                Button(31, Material.COMPARATOR, Main.Gui.Button.SETTINGS.t(p), mainBoardKey, CustomID.SETTINGS.name),
//                Button(33, Material.OAK_SIGN, Main.Gui.Button.HELP.t(p), mainBoardKey, CustomID.HELP.name)
            ),
            // Dynamic
            GUIState.NEW_POST to listOf(

            ),
            // Page + Dynamic
            GUIState.MY_POSTS to listOf(

            ),
            // Page + Dynamic
            GUIState.ALL_POSTS to listOf(

            ),
            //TODO: ADD MORE GUIs !!! :D
        )

        val pageChangeButton = Pair(
            Button(
                18,
                Material.ARROW,
                ap.getMessage(GUI.Buttons.Paged.Previous)
            ),
            Button(26,
                Material.ARROW,
                ap.getMessage(GUI.Buttons.Paged.Next)
            )
        )

        val noPosts = Button(13, Material.PAPER, ap.getMessage(GUI.Other.NoPosts))

        val gui = PaginatedDynamicGUI.of<GUIState>(player)
            .setStateButtonMapping(buttonMapping)
            .setSlotPositions(middleRowSlots)
            .setPageItems(postButtons)
            .setItemsPerPage(middleRowSlots.size)
            .setEmptyMessageButton(noPosts)
            .setPageButtons(pageChangeButton.first, pageChangeButton.second)
            .setState(GUIState.MAIN_BOARD)
            // End of the PaginatedGUI configuration
            .setSize(27)
            .setTitle(ap.getMessage(GUI.Title))
            .setBackground(Material.GRAY_STAINED_GLASS_PANE)
            .build()

        gui.open()
    }
}
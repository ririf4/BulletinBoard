package net.ririfa.bulletinboard.gui

import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.DB
import net.ririfa.bulletinboard.Plugin
import net.ririfa.bulletinboard.gui.GUIState.*
import net.ririfa.bulletinboard.toDraft
import net.ririfa.bulletinboard.translation.BBMessageKey.GUI
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.PageButtons
import net.ririfa.bulletinboard.util.displayPost
import net.ririfa.bulletinboard.util.getPlayerState
import net.ririfa.igf.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent

object GUIManager {
    private val paginationEnableState = setOf(
        ALL_POSTS,
        MY_POSTS,
        DELETED_POSTS,

        EDIT_POST_SELECTION,

        DELETE_POST_SELECTION,
        DELETE_POST_OTHERS_SELECTION,
        DELETE_POST_PERMANENTLY_SELECTION,

        RESTORE_POST_SELECTION,
    )

    private val middleRowSlots = listOf(10, 12, 14, 16)

    fun openGUI(player: Player, state: GUIState = MAIN_BOARD) {
        if (state == MAIN_BOARD) {
            openMain(player); return
        }

        val pageChangeButtons = getPageChangeButtons(player)

        val gui = PaginatedDynamicGUI.of<GUIState>(player)
            .setStateFixedButtonProviders(GUIState.getProviderMap(player))
            .setPaginationEnabledStates(paginationEnableState)
            .setSlotPositions(middleRowSlots)
            .setPageItemProvider { state -> resolvePageButtons(state, player, middleRowSlots) }
            .setItemsPerPage(middleRowSlots.size)
            .setEmptyMessageButton(getNoPostButton(player))
            .setPageChangeButtons(pageChangeButtons.previousPage, pageChangeButtons.nextPage)
            .onClose { _, reason -> if (reason == InventoryCloseEvent.Reason.PLAYER) player.getPlayerState().reset() }
            .setState(state)
            .setSize(27)
            .setTitle(getGUITitle(player))
            .setBackground(Material.GRAY_STAINED_GLASS_PANE)
            .build()

        gui.open()
    }

    fun openMain(player: Player) {
        val titleAc = mapOf("version" to Plugin.version)
        val ap = player.adapt()

        val gui = PaginatedDynamicGUI.of<SinglePage>(player)
            .setStateFixedButtonProviders(
                mapOf(
                SinglePage.PAGE to { _: SinglePage ->
                    MAIN_BOARD.provider(player)
                }
            ))
            .onClose { _, reason ->
                // If player closes gui, all state will clear
                if (reason == InventoryCloseEvent.Reason.PLAYER) player.getPlayerState().reset()
            }
            .setState(SinglePage.PAGE)
            .setTitle(ap.getMessage(GUI.Title, titleAc))
            .setSize(45)
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
            ALL_POSTS -> {
                DB.getAllPosts()
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClick { player.displayPost(post) }
                        }
                    }
            }
            MY_POSTS -> {
                DB.getMyPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClick { player.displayPost(post) }
                        }
                    }
            }
            DELETED_POSTS -> {
                DB.getDeletedPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClick { player.displayPost(post) }
                        }
                    }
            }

            EDIT_POST_SELECTION -> {
                DB.getMyPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, gui ->
                                    player.getPlayerState().draft = post.toDraft()
                                    gui.switchState(EDIT_POST)
                                }
                        }
                    }
            }

            DELETE_POST_SELECTION -> {
                DB.getMyPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, gui ->
                                    player.getPlayerState().selectedPostId = post.id
                                    gui.switchState(CONFIRM_DELETE_POST)
                                }
                        }
                    }
            }

            RESTORE_POST_SELECTION -> {
                DB.getDeletedPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, gui ->
                                    player.getPlayerState().selectedPostId = post.id
                                    gui.switchState(CONFIRM_RESTORE_POST)
                                }
                        }
                    }
            }

            DELETE_POST_PERMANENTLY_SELECTION -> {
                DB.getDeletedPost()
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, gui ->
                                    player.getPlayerState().selectedPostId = post.id
                                    gui.switchState(CONFIRM_DELETE_POST_PERMANENTLY)
                                }
                        }
                    }
            }

            else -> emptyList()
        }
    }

    private fun getPageChangeButtons(player: Player): PageButtons {
        val ap = player.adapt()
        return PageButtons(
            Button(
                18,
                Material.ARROW,
                ap.getMessage(GUI.Buttons.Pagination.Previous)
            ),
            Button(
                26,
                Material.ARROW,
                ap.getMessage(GUI.Buttons.Pagination.Next)
            )
        )
    }

    private fun getNoPostButton(player: Player): Button {
        val ap = player.adapt()
        return Button(13, Material.PAPER, ap.getMessage(GUI.Other.NoPosts))
    }

    private fun getGUITitle(player: Player): Component {
        val ap = player.adapt()
        val titleAC = mapOf("version" to Plugin.version)
        return ap.getMessage(GUI.Title, titleAC)
    }
}
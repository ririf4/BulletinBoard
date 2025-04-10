package net.ririfa.bulletinboard.gui

import net.ririfa.bulletinboard.DataBase
import net.ririfa.bulletinboard.gui.GUIState.*
import net.ririfa.bulletinboard.translation.BBMessageKey.GUI
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.*
import net.ririfa.igf.Button
import net.ririfa.igf.PaginatedDynamicGUI
import net.ririfa.igf.setClick
import net.ririfa.igf.setClickTyped
import org.bukkit.Material
import org.bukkit.entity.Player

object GUIManager {
    fun openGUI(player: Player, openState: GUIState = MAIN_BOARD) {
        val ap = player.adapt()
        val middleRowSlots = listOf(10, 12, 14, 16)

        val fixedButtonProvider = mapOf<GUIState, (GUIState) -> List<Button>>(
            MAIN_BOARD to { state ->
                listOf(
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
                        }
                )
            },

            NEW_POST to { state ->
                val playerState = player.getPlayerState()
                val draft = playerState.draftState.draft ?: PostDraft(
                    title = GUI.Editor.NoTitle.t(ap),
                    content = GUI.Editor.NoContent.t(ap)
                )
                playerState.draftState.draft = draft

                val title = draft.title
                val content = draft.content

                listOf(
                    Button(11, Material.PAPER, title)
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            playerState.inputState.inputType = InputType.TITLE
                            playerState.inputState.isInputting = true
                            gui.close()
                            player.sendMessage(ap.getMessage(GUI.Messages.EnterTitle))
                        },

                    Button(15, Material.BOOK, content)
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            playerState.inputState.inputType = InputType.CONTENT
                            playerState.inputState.isInputting = true
                            gui.close()
                            player.sendMessage(ap.getMessage(GUI.Messages.EnterContent))
                        },

                    Button(19, Material.RED_WOOL, GUI.Editor.Cancel.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            playerState.clearDraft()
                            gui.switchState(MAIN_BOARD)
                        },

                    Button(25, Material.GREEN_WOOL, GUI.Editor.Save.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.switchState(CONFIRM_SAVE_POST)
                        }
                )
            },

            EDIT_POST to { state ->
                val playerState = player.getPlayerState()
                val draft = playerState.draftState.editDraft

                if (draft == null) {
                    return@to listOf(
                        Button(13, Material.BARRIER, GUI.Editor.EditError.t(ap))
                    )
                }

                listOf(
                    Button(11, Material.PAPER, draft.title)
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            playerState.inputState.editInputType = InputType.TITLE
                            playerState.inputState.isEditInputting = true
                            gui.close()
                            player.sendMessage(ap.getMessage(GUI.Messages.EnterTitleEdit))
                        },

                    Button(15, Material.BOOK, draft.content)
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            playerState.inputState.editInputType = InputType.CONTENT
                            playerState.inputState.isEditInputting = true
                            gui.close()
                            player.sendMessage(ap.getMessage(GUI.Messages.EnterContentEdit))
                        },

                    Button(19, Material.RED_WOOL, GUI.Editor.Cancel.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            playerState.draftState.editDraft = null
                            gui.switchState(MY_POSTS)
                        },

                    Button(25, Material.GREEN_WOOL, GUI.Editor.Save.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.switchState(CONFIRM_SAVE_EDIT)
                        }
                )
            },


            MY_POSTS to { state ->
                listOf(
                    Button(20, Material.WRITABLE_BOOK, GUI.Buttons.MyPosts.EditPost.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.switchState(EDIT_POST_SELECTION)
                        },
                    Button(24, Material.CAULDRON, GUI.Buttons.MyPosts.DeletePost.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.switchState(DELETE_POST_SELECTION)
                        }
                )
            },

            // Page + Dynamic
            ALL_POSTS to { state ->
                buildList {
                    if (player.hasPermission("bulletinboard.post.delete.other")) {
                        add(
                            Button(20, Material.RED_WOOL, GUI.Buttons.AllPosts.DeleteOthers.t(ap))
                                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                                    gui.switchState(DELETE_POST_OTHERS_SELECTION)
                                }
                        )
                    }
                }
            },

            DELETED_POSTS to { state ->
                listOf(
                    Button(20, Material.RESPAWN_ANCHOR, GUI.Buttons.DeletedPosts.RestorePost.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.switchState(RESTORE_POST_SELECTION)
                        },
                    Button(24, Material.LAVA_BUCKET, GUI.Buttons.DeletedPosts.DeletePostPermanently.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.switchState(DELETE_POST_PERMANENTLY_SELECTION)
                        }
                )
            }
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
            .setStateFixedButtonProviders(fixedButtonProvider)
            .setSlotPositions(middleRowSlots)
            .setPageItemProvider { state -> resolvePageButtons(state, player, middleRowSlots) }
            .setItemsPerPage(middleRowSlots.size)
            .setEmptyMessageButton(noPosts)
            .setPageChangeButtons(pageChangeButton.first, pageChangeButton.second)
            .setState(openState)
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

            EDIT_POST_SELECTION -> {
                DataBase.Accessor.getMyPosts(player)
                    .mapIndexedNotNull { index, post ->
                        middleRowSlots.getOrNull(index)?.let { slot ->
                            Button(slot, Material.WRITTEN_BOOK, post.title)
                                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                                    val state = player.getPlayerState()
                                    state.draftState.editDraft = EditPostData(
                                        id = post.id,
                                        title = post.title,
                                        content = post.content,
                                        isAnonymous = post.isAnonymous
                                    )
                                    gui.switchState(EDIT_POST)
                                }
                        }
                    }
            }

            DELETE_POST_SELECTION -> TODO()
            DELETE_POST_OTHERS_SELECTION -> TODO()
            RESTORE_POST_SELECTION -> TODO()
            DELETE_POST_PERMANENTLY_SELECTION -> TODO()
            CONFIRM_SAVE_POST -> TODO()
            EDIT_POST -> TODO()
            CONFIRM_SAVE_EDIT -> TODO()
        }
    }
}
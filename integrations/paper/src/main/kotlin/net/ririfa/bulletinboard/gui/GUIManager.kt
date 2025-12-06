package net.ririfa.bulletinboard.gui

import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.DB
import net.ririfa.bulletinboard.Logger
import net.ririfa.bulletinboard.Plugin
import net.ririfa.bulletinboard.gui.GUIState.*
import net.ririfa.bulletinboard.translation.BBMessageKey.GUI
import net.ririfa.bulletinboard.translation.BBMessageKey.Messages
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.*
import net.ririfa.igf.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent

object GUIManager {
    private val paginationEnableState = setOf(
        ALL_POSTS,
        MY_POSTS,
        DELETED_POSTS
    )

    private val middleRowSlots = listOf(10, 12, 14, 16)

    fun openGUI(player: Player, state: GUIState = MAIN_BOARD) {
        if (state == MAIN_BOARD) {
            openMain(player); return
        }

        val pageChangeButtons = getPageChangeButtons(player)

        val gui = PaginatedDynamicGUI.of<GUIState>(player)
            .setStateFixedButtonProviders(getFixedButtonProviders(player))
            .setPaginationEnabledStates(paginationEnableState)
            .setSlotPositions(middleRowSlots)
            .setPageItemProvider { state -> resolvePageButtons(state, player, middleRowSlots) }
            .setItemsPerPage(middleRowSlots.size)
            .setEmptyMessageButton(getNoPostButton(player))
            .setPageChangeButtons(pageChangeButtons.previousPage, pageChangeButtons.nextPage)
            .onClose { _, reason ->
                if (reason == InventoryCloseEvent.Reason.PLAYER) player.getPlayerState().reset()
            }
            .setState(state)
            .setSize(27)
            .setTitle(getTitle(player))
            .setBackground(Material.GRAY_STAINED_GLASS_PANE)
            .build()

        gui.open()
    }

    private fun openMain(player: Player) {
        val titleAc = mapOf(
            "version" to Plugin.version
        )
        val ap = player.adapt()
        val fixedButtonProvider = mapOf<SinglePage, (SinglePage) -> List<Button>>(
            SinglePage.PAGE to { state ->
                listOf(
                    Button(10, Material.WRITABLE_BOOK, GUI.Buttons.MainBoard.NewPost.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, gui ->
                            gui.close()
                            openGUI(player, NEW_POST)
                        },
                    Button(12, Material.BOOK, GUI.Buttons.MainBoard.AllPosts.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.close()
                            openGUI(player, ALL_POSTS)
                        },
                    Button(14, Material.WRITTEN_BOOK, GUI.Buttons.MainBoard.MyPosts.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.close()
                            openGUI(player, MY_POSTS)
                        },
                    Button(16, Material.CAULDRON, GUI.Buttons.MainBoard.DeletedPosts.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.close()
                            openGUI(player, DELETED_POSTS)
                        },
                    Button(28, Material.LECTERN, GUI.Buttons.MainBoard.AboutPlugin.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.close()
                            displayAbout(player)
                        },
                    Button(30, Material.COMPARATOR, GUI.Buttons.MainBoard.Settings.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.close()
                            openGUI(player, SETTINGS)
                        },
                    Button(32, Material.OAK_SIGN, GUI.Buttons.MainBoard.Help.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.close()
                            displayHelp(player)
                        },
                    Button(34, Material.ENCHANTED_GOLDEN_APPLE, GUI.Buttons.MainBoard.Discord.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.close()
                            showDiscordLink(player)
                        }
                )
            }
        )

        val gui = PaginatedDynamicGUI.of<SinglePage>(player)
            .setStateFixedButtonProviders(fixedButtonProvider)
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

    private fun getFixedButtonProviders(player: Player): Map<GUIState, (GUIState) -> List<Button>> {
        val ap = player.adapt()
        val ps = player.getPlayerState()

        return mapOf(

            //----------------------------------------------------------------------
            // NEW_POST（新規投稿画面）
            //----------------------------------------------------------------------
            NEW_POST to { _ ->
                val draft = ps.draft ?: PostDraft(
                    id = ShortUUID.generate(),
                    title = GUI.Editor.NoTitle.t(ap),
                    content = GUI.Editor.NoContent.t(ap)
                )
                ps.draft = draft

                listOf(
                    // タイトル編集
                    Button(11, Material.PAPER, draft.title)
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            ps.mode = PlayerMode.AWAITING_TITLE_INPUT
                            gui.close()
                            player.sendMessage(ap.getMessage(Messages.Edit.EnterTitle))
                        },

                    // 内容編集
                    Button(15, Material.BOOK, draft.content)
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            ps.mode = PlayerMode.AWAITING_CONTENT_INPUT
                            gui.close()
                            player.sendMessage(ap.getMessage(Messages.Edit.EnterContent))
                        },

                    // キャンセル
                    Button(19, Material.RED_WOOL, GUI.Editor.Cancel.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            ps.reset()
                            gui.switchState(MAIN_BOARD)
                        },

                    // 保存（確認画面へ）
                    Button(25, Material.GREEN_WOOL, GUI.Editor.Save.t(ap))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            ps.mode = PlayerMode.CONFIRM_CREATING_POST
                            gui.switchState(CONFIRM_SAVE_POST)
                        }
                )
            },

            //----------------------------------------------------------------------
            // EDIT_POST（投稿編集画面）
            //----------------------------------------------------------------------
//            EDIT_POST to { _ ->
//
//                val draft = ps.draft
//                if (draft == null) {
//                    return@to listOf(
//                        Button(13, Material.BARRIER,
//                            ap.getMessage(GUI.Editor.EditError, mapOf(
//                                "reason" to ap.getMessage(GUI.Editor.EditErrorReason.DraftNull)
//                            ))
//                        )
//                    )
//                }
//
//                listOf(
//                    // タイトル編集
//                    Button(11, Material.PAPER, draft.title)
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            ps.mode = PlayerMode.AWAITING_EDIT_TITLE_INPUT
//                            gui.close()
//                            player.sendMessage(ap.getMessage(GUI.Messages.EnterTitleEdit))
//                        },
//
//                    // 内容編集
//                    Button(15, Material.BOOK, draft.content)
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            ps.mode = PlayerMode.AWAITING_EDIT_CONTENT_INPUT
//                            gui.close()
//                            player.sendMessage(ap.getMessage(GUI.Messages.EnterContentEdit))
//                        },
//
//                    // キャンセル
//                    Button(19, Material.RED_WOOL, GUI.Editor.CancelEdit.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            ps.reset()
//                            gui.switchState(MY_POSTS)
//                        },
//
//                    // 保存（確認画面へ）
//                    Button(25, Material.GREEN_WOOL, GUI.Editor.SaveEdit.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            ps.mode = PlayerMode.CONFIRM_EDITING_POST
//                            gui.switchState(CONFIRM_SAVE_EDIT)
//                        }
//                )
//            },

            //----------------------------------------------------------------------
            // MY_POSTS（自分の投稿管理）
            //----------------------------------------------------------------------
//            MY_POSTS to { _ ->
//                listOf(
//                    Button(20, Material.WRITABLE_BOOK, GUI.Buttons.MyPosts.EditPost.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            gui.switchState(EDIT_POST_SELECTION)
//                        },
//
//                    Button(24, Material.CAULDRON, GUI.Buttons.MyPosts.DeletePost.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            gui.switchState(DELETE_POST_SELECTION)
//                        },
//
//                    Button(22, Material.BARRIER, ap.getMessage(GUI.Buttons.BackButton))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, _ ->
//                            openGUI(player, MAIN_BOARD)
//                        }
//                )
//            },

            //----------------------------------------------------------------------
            // ALL_POSTS（全投稿一覧）
            //----------------------------------------------------------------------
//            ALL_POSTS to { _ ->
//                buildList {
//                    if (player.hasPermission("bulletinboard.post.delete.other")) {
//                        add(
//                            Button(20, Material.RED_WOOL, GUI.Buttons.AllPosts.DeleteOthers.t(ap))
//                                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                                    gui.switchState(DELETE_POST_OTHERS_SELECTION)
//                                }
//                        )
//                    }
//                    add(
//                        Button(22, Material.BARRIER, ap.getMessage(GUI.Buttons.BackButton))
//                            .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, _ ->
//                                openGUI(player, MAIN_BOARD)
//                            }
//                    )
//                }
//            },

            //----------------------------------------------------------------------
            // DELETED_POSTS（削除済み投稿一覧）
            //----------------------------------------------------------------------
//            DELETED_POSTS to { _ ->
//                listOf(
//                    Button(20, Material.RESPAWN_ANCHOR, GUI.Buttons.DeletedPosts.RestorePost.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            gui.switchState(RESTORE_POST_SELECTION)
//                        },
//
//                    Button(24, Material.LAVA_BUCKET, GUI.Buttons.DeletedPosts.DeletePostPermanently.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            gui.switchState(DELETE_POST_PERMANENTLY_SELECTION)
//                        },
//
//                    Button(22, Material.BARRIER, ap.getMessage(GUI.Buttons.BackButton))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, _ ->
//                            openGUI(player, MY_POSTS)
//                        }
//                )
//            },

            //----------------------------------------------------------------------
            // CONFIRM_SAVE_POST（新規投稿の保存確認）
            //----------------------------------------------------------------------
//            CONFIRM_SAVE_POST to { _ ->
//                listOf(
//                    // キャンセル
//                    Button(11, Material.RED_WOOL, GUI.Buttons.Confirmation.CancelCancelPost.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            ps.mode = PlayerMode.CREATING_POST
//                            gui.switchState(NEW_POST)
//                        },
//
//                    // 確定（保存処理）
//                    Button(15, Material.GREEN_WOOL, GUI.Buttons.Confirmation.Confirm.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            val draft = ps.draft ?: return@setClickTyped
//                            val post = DataBase.Post(
//                                id = ShortUUID.generate(),
//                                title = draft.title,
//                                content = draft.content,
//                                author = player.uniqueId,
//                                isAnonymous = draft.isAnonymous ?: false,
//                                date = java.util.Date(),
//                                isDeleted = false
//                            )
//
//                            Plugin.execute {
//                                DataBase.Accessor.insertPost(post)
//                            }
//
//                            player.sendMessage(ap.getMessage(GUI.Messages.PostSaved))
//                            ps.reset()
//                            gui.close()
//                        }
//                )
//            },

            //----------------------------------------------------------------------
            // CONFIRM_SAVE_EDIT（編集投稿の保存確認）
            //----------------------------------------------------------------------
//            CONFIRM_SAVE_EDIT to { _ ->
//                listOf(
//                    Button(11, Material.RED_WOOL, GUI.Buttons.Confirmation.CancelEdit.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            ps.mode = PlayerMode.EDITING_POST
//                            gui.switchState(EDIT_POST)
//                        },
//
//                    Button(15, Material.GREEN_WOOL, GUI.Buttons.Confirmation.Confirm.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            val draft = ps.draft ?: return@setClickTyped
//
//                            val id = ps.selectedPostId
//                            if (id == null) {
//                                player.sendMessage(ap.getMessage(GUI.Messages.PostDraftNull))
//                                ps.reset()
//                                gui.close()
//                                return@setClickTyped
//                            }
//
//                            Plugin.execute {
//                                DataBase.Accessor.updatePost(id, draft)
//                            }
//
//                            ps.reset()
//                            gui.close()
//                            player.sendMessage(ap.getMessage(GUI.Messages.PostEdited))
//                        }
//                )
//            },

            //----------------------------------------------------------------------
            // CONFIRM_CANCEL_POST（キャンセル）
            //----------------------------------------------------------------------
//            CONFIRM_CANCEL_POST to { _ ->
//                listOf(
//                    Button(13, Material.BARRIER, GUI.Buttons.Confirmation.CancelCancelPost.t(ap))
//                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
//                            ps.reset()
//                            gui.switchState(MAIN_BOARD)
//                        }
//                )
//            }
        )
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
                                .setClick { Logger.info("click"); displayPost(player, post) }
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

    private fun getTitle(player: Player): Component {
        val ap = player.adapt()
        val titleAC = mapOf(
            "version" to Plugin.version
        )
        return ap.getMessage(GUI.Title, titleAC)
    }
}
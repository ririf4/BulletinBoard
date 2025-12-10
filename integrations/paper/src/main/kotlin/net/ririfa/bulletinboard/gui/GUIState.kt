package net.ririfa.bulletinboard.gui

import net.ririfa.bulletinboard.DataBase
import net.ririfa.bulletinboard.gui.GUIManager.openGUI
import net.ririfa.bulletinboard.gui.GUIManager.openMain
import net.ririfa.bulletinboard.translation.BBMessageKey.GUI
import net.ririfa.bulletinboard.translation.BBMessageKey.Messages
import net.ririfa.bulletinboard.translation.t
import net.ririfa.bulletinboard.util.*
import net.ririfa.igf.Button
import net.ririfa.igf.PaginatedDynamicGUI
import net.ririfa.igf.setClick
import net.ririfa.igf.setClickTyped
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

enum class GUIState(val provider: (Player) -> List<Button>) {
    MAIN_BOARD({ player ->
        listOf(
            Button(10, Material.WRITABLE_BOOK, GUI.Buttons.MainBoard.NewPost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, gui ->
                    gui.close()
                    openGUI(player, NEW_POST)
                },
            Button(12, Material.BOOK, GUI.Buttons.MainBoard.AllPosts.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.close()
                    openGUI(player, ALL_POSTS)
                },
            Button(14, Material.WRITTEN_BOOK, GUI.Buttons.MainBoard.MyPosts.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.close()
                    openGUI(player, MY_POSTS)
                },
            Button(16, Material.CAULDRON, GUI.Buttons.MainBoard.DeletedPosts.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.close()
                    openGUI(player, DELETED_POSTS)
                },
            Button(28, Material.LECTERN, GUI.Buttons.MainBoard.AboutPlugin.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.close()
                    displayAbout(player)
                },
            Button(30, Material.COMPARATOR, GUI.Buttons.MainBoard.Settings.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.close()
                    openGUI(player, SETTINGS)
                },
            Button(32, Material.OAK_SIGN, GUI.Buttons.MainBoard.Help.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.close()
                    displayHelp(player)
                },
            Button(34, Material.ENDER_EYE, GUI.Buttons.MainBoard.Discord.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.close()
                    showDiscordLink(player)
                }
        )
    }),
    NEW_POST({ player ->
        val ps = player.getPlayerState()
        val draft = ps.draft ?: PostDraft(
            id = ShortUUID.generate(),
            title = GUI.Editor.NoTitle.t(player),
            content = GUI.Editor.NoContent.t(player),
            isAnonymous = false,
            date = Date()
        )
        ps.draft = draft

        listOf(
            // タイトル編集
            Button(11, Material.PAPER, draft.title)
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.AWAITING_TITLE_INPUT
                    gui.close()
                    player.sendMessage(Messages.Edit.EnterTitle.t(player))
                },

            // 内容編集
            Button(15, Material.BOOK, draft.content)
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.AWAITING_CONTENT_INPUT
                    gui.close()
                    player.sendMessage(Messages.Edit.EnterContent.t(player))
                },

            // キャンセル
            Button(19, Material.RED_WOOL, GUI.Editor.Cancel.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { p, _ ->
                    ps.reset()
                    openMain(p)
                },

            // 保存（確認画面へ）
            Button(25, Material.GREEN_WOOL, GUI.Editor.Save.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.CONFIRM_CREATING_POST
                    gui.switchState(CONFIRM_SAVE_POST)
                }
        )
    }),
    EDIT_POST({ player ->
        val ps = player.getPlayerState()
        val draft = ps.draft!! // EDIT_POST_SELECTIONで設定済み

        listOf(
            // タイトル編集
            Button(11, Material.PAPER, draft.title)
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.AWAITING_EDIT_TITLE_INPUT
                    gui.close()
                    player.sendMessage(Messages.Edit.EnterTitleEdit.t(player))
                },

            // 内容編集
            Button(15, Material.BOOK, draft.content)
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.AWAITING_EDIT_CONTENT_INPUT
                    gui.close()
                    player.sendMessage(Messages.Edit.EnterContentEdit.t(player))
                },

            // キャンセル
            Button(19, Material.RED_WOOL, GUI.Editor.CancelEdit.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.reset()
                    gui.switchState(MY_POSTS)
                },

            // 保存（確認画面へ）
            Button(25, Material.GREEN_WOOL, GUI.Editor.SaveEdit.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.CONFIRM_EDITING_POST
                    gui.switchState(CONFIRM_SAVE_EDIT)
                }
        )
    }),
    MY_POSTS({ player ->
        listOf(
            Button(20, Material.WRITABLE_BOOK, GUI.Buttons.MyPosts.EditPost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, gui ->
                    player.getPlayerState().mode = PlayerMode.SELECTING_EDIT_TARGET
                    gui.switchState(EDIT_POST_SELECTION)
                },

            Button(24, Material.CAULDRON, GUI.Buttons.MyPosts.DeletePost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    player.getPlayerState().mode = PlayerMode.SELECTING_DELETE_TARGET
                    gui.switchState(DELETE_POST_SELECTION)
                },

            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, _ ->
                    player.getPlayerState().reset()
                    openGUI(player, MAIN_BOARD)
                }
        )
    }),
    ALL_POSTS({ player ->
        buildList {
            if (player.hasPermission(PermEnum.POST_DELETE_OTHER.permissionNode)) {
                add(
                    Button(20, Material.RED_WOOL, GUI.Buttons.AllPosts.DeleteOthers.t(player))
                        .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                            gui.switchState(DELETE_POST_OTHERS_SELECTION)
                        }
                )
            }
            add(
                Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                    .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, _ ->
                        openGUI(player, MAIN_BOARD)
                    }
            )
        }
    }),
    DELETED_POSTS({ player ->
        listOf(
            Button(20, Material.RESPAWN_ANCHOR, GUI.Buttons.DeletedPosts.RestorePost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.switchState(RESTORE_POST_SELECTION)
                },

            Button(24, Material.LAVA_BUCKET, GUI.Buttons.DeletedPosts.DeletePostPermanently.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.switchState(DELETE_POST_PERMANENTLY_SELECTION)
                },

            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { player, _ ->
                    openMain(player)
                }
        )
    }),
    SETTINGS({ player ->
        listOf(
            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { p, _ ->
                    openGUI(p, MAIN_BOARD)
                }
        )
    }),

    EDIT_POST_SELECTION({ player ->
        listOf(
            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { p, gui ->
                    p.getPlayerState().mode = PlayerMode.NONE
                    gui.switchState(MY_POSTS)
                }
        )
    }),
    DELETE_POST_SELECTION({ player ->
        listOf(
            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { p, gui ->
                    p.getPlayerState().mode = PlayerMode.NONE
                    gui.switchState(MY_POSTS)
                }
        )
    }),
    RESTORE_POST_SELECTION({ player ->
        listOf(
            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { p, gui ->
                    p.getPlayerState().mode = PlayerMode.NONE
                    gui.switchState(DELETED_POSTS)
                }
        )
    }),
    DELETE_POST_PERMANENTLY_SELECTION({ player ->
        listOf(
            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { p, gui ->
                    p.getPlayerState().mode = PlayerMode.NONE
                    gui.switchState(DELETED_POSTS)
                }
        )
    }),

    DELETE_POST_OTHERS_SELECTION({ player ->
        listOf(
            Button(22, Material.BARRIER, GUI.Buttons.BackButton.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    gui.switchState(ALL_POSTS)
                }
        )
    }),

    CONFIRM_SAVE_POST({ player ->
        val ps = player.getPlayerState()
        listOf(
            // キャンセル
            Button(11, Material.RED_WOOL, GUI.Buttons.Confirmation.CancelSavePost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.CREATING_POST
                    gui.switchState(NEW_POST)
                },

            // 確定（保存処理）
            Button(15, Material.GREEN_WOOL, GUI.Buttons.Confirmation.ConfirmSavePost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    val draft = ps.draft ?: return@setClickTyped
                    val post = DataBase.Post(
                        id = ShortUUID.generate(),
                        title = draft.title,
                        content = draft.content,
                        author = player.uniqueId,
                        isAnonymous = draft.isAnonymous,
                        date = Date(),
                        editedOn = Date(),
                        isDeleted = false
                    )

                    DataBase.insertPost(post)

                    player.sendMessage(Messages.PostSaved.t(player))
                    ps.reset()
                    gui.close()
                }
        )
    }),
    CONFIRM_SAVE_EDIT({ player ->
        val ps = player.getPlayerState()
        listOf(
            Button(11, Material.RED_WOOL, GUI.Buttons.Confirmation.CancelSaveEdit.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.EDITING_POST
                    gui.switchState(EDIT_POST)
                },

            Button(15, Material.GREEN_WOOL, GUI.Buttons.Confirmation.ConfirmSaveEdit.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    val draft = ps.draft ?: run {
                        player.sendMessage(Messages.PostDraftNull.t(player))
                        ps.reset()
                        gui.close()
                        return@setClickTyped
                    }

                    DataBase.updatePost(draft.toPost(player))

                    ps.reset()
                    gui.close()
                    player.sendMessage(Messages.PostEdited.t(player))
                }
        )
    }),

    CONFIRM_CANCEL_POST({ player ->
        val ps = player.getPlayerState()
        listOf(
            Button(11, Material.RED_WOOL, GUI.Buttons.Confirmation.CancelCancelPost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.CREATING_POST
                    gui.switchState(NEW_POST)
                },

            Button(15, Material.GREEN_WOOL, GUI.Buttons.Confirmation.ConfirmCancelPost.t(player))
                .setClick {
                    ps.reset()
                    openMain(player)
                }
        )
    }),
    CONFIRM_CANCEL_EDIT({ player ->
        val ps = player.getPlayerState()
        listOf(
            Button(11, Material.RED_WOOL, GUI.Buttons.Confirmation.CancelCancelEdit.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.EDITING_POST
                    gui.switchState(EDIT_POST)
                },

            Button(15, Material.GREEN_WOOL, GUI.Buttons.Confirmation.ConfirmCancelEdit.t(player))
                .setClick {
                    ps.reset()
                    openMain(player)
                }
        )
    }),

    CONFIRM_DELETE_POST({ player ->
        val ps = player.getPlayerState()
        listOf(
            Button(11, Material.RED_WOOL, GUI.Buttons.Confirmation.CancelDeletePost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    ps.mode = PlayerMode.NONE
                    gui.switchState(MY_POSTS)
                },

            Button(15, Material.GREEN_WOOL, GUI.Buttons.Confirmation.ConfirmDeletePost.t(player))
                .setClickTyped<PaginatedDynamicGUI<GUIState>> { _, gui ->
                    val id = ps.selectedPostId ?: run {
                        player.sendMessage(Messages.SelectedPostNotFound.t(player))
                        ps.reset()
                        openGUI(player, MAIN_BOARD)
                        return@setClickTyped
                    }
                    DataBase.deletePost(id)
                    ps.reset()
                    player.sendMessage(Messages.PostDeleted.t(player))
                    gui.close()
                }
        )
    }),
    CONFIRM_RESTORE_POST({ player ->
        val ps = player.getPlayerState()
        listOf()
    }),

    CONFIRM_DELETE_POST_PERMANENTLY({ player ->
        val ps = player.getPlayerState()
        listOf()
    })
    ;

    companion object {
        fun getProviderMap(player: Player): Map<GUIState, (GUIState) -> List<Button>> {
            return entries.associateWith { state -> { _: GUIState -> state.provider(player) } }
        }
    }
}
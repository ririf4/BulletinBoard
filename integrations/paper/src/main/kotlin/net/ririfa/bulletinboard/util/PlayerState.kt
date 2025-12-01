package net.ririfa.bulletinboard.util

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val playerState = ConcurrentHashMap<UUID, PlayerState>()

data class PlayerState(
    var mode: PlayerMode = PlayerMode.NONE,
    var draft: PostDraft? = null,
    var selectedPostId: ShortUUID? = null,

    var previewBeforeMode: PlayerMode? = null
) {
    fun reset() {
        mode = PlayerMode.NONE
        draft = null
        selectedPostId = null
    }
}

enum class PlayerMode {
    NONE,

    // Creating
    CREATING_POST, // 投稿を作る
    AWAITING_TITLE_INPUT, // タイトル入力待ち
    AWAITING_CONTENT_INPUT, // 内容入力待ち
    CONFIRM_CREATING_POST, // 投稿確認

    // Editing
    SELECTING_EDIT_TARGET, // 編集する投稿を選択
    EDITING_POST, // 投稿を編集する
    AWAITING_EDIT_TITLE_INPUT, // タイトル入力待ち
    AWAITING_EDIT_CONTENT_INPUT, // 内容入力待ち
    CONFIRM_EDITING_POST, // 編集確認

    // Deleting
    SELECTING_DELETE_TARGET, // 削除する投稿を選択
    CONFIRM_DELETE_POST, // 削除確認

    // Restoring
    SELECTING_RESTORE_TARGET, // 復元する投稿を選択
    CONFIRM_RESTORE_POST, // 復元確認

    PREVIEWING_POST // 投稿をプレビュー中
}

data class PostDraft(
    var id: ShortUUID,
    var title: Component,
    var content: Component,
    var isAnonymous: Boolean = false
)

fun Player.getPlayerState(): PlayerState {
    return playerState.computeIfAbsent(this.uniqueId) { PlayerState() }
}
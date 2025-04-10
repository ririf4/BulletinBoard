package net.ririfa.bulletinboard.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val playerState = ConcurrentHashMap<UUID, PlayerState>()

data class PlayerState(
    var draftState: DraftState = DraftState(),
    var selectionState: SelectionState = SelectionState(),
    var inputState: InputState = InputState(),
    var confirmationState: ConfirmationState = ConfirmationState()
) {
    fun clearAll() {
        draftState = DraftState()
        selectionState = SelectionState()
        inputState = InputState()
        confirmationState = ConfirmationState()
    }

    fun clearDraft() {
        draftState = DraftState()
    }

    fun clearInput() {
        inputState = InputState()
    }
}


data class DraftState(
    var draft: PostDraft? = null,
    var editDraft: EditPostData? = null
)

data class SelectionState(
    var deletingPostId: String? = null,
    var restoringPostId: String? = null
)

data class InputState(
    var isInputting: Boolean? = null,
    var inputType: InputType? = null,
    var isEditInputting: Boolean? = null,
    var editInputType: InputType? = null
)

data class ConfirmationState(
    var isOpening: Boolean? = null,
    var isChoosingAnswer: Boolean? = null,
    var type: ConfirmationType? = null,
    var preview: Pair<TextComponent, TextComponent>? = null
)

interface Draft {
    var title: Component
    var content: Component
    var isAnonymous: Boolean?
}

data class PostDraft(
    override var title: Component,
    override var content: Component,
    override var isAnonymous: Boolean? = null
) : Draft

data class EditPostData(
    val id: ShortUUID? = null,
    override var title: Component,
    override var content: Component,
    override var isAnonymous: Boolean? = null
) : Draft {
    fun toPost(author: UUID, date: Date): Post {
        return Post(
            id = this.id ?: ShortUUID.generate(),
            title = this.title,
            author = author,
            content = this.content,
            isAnonymous = this.isAnonymous ?: false,
            date = date,
            // 削除済みの投稿は編集できない
            isDeleted = false
        )
    }
}

enum class InputType {
    TITLE,
    CONTENT,
    NONE
}

enum class ConfirmationType {
    // 投稿するんか？
    SAVE_POST,

    // 投稿キャンセル？
    CANCEL_POST,

    // 編集確定？
    SAVE_EDIT,

    // やっぱ編集辞める？
    CANCEL_EDIT,

    // 投稿消すんか？
    DELETE_POST,

    // 投稿永久削除するで？
    DELETE_POST_PERMANENTLY,

    // やっぱ復元するんか？
    RESTORE_POST,

    // 他プレイヤーの投稿やぞ？
    DELETE_POST_FROM_ALL,
}

fun Player.getPlayerState(): PlayerState {
    return playerState.computeIfAbsent(this.uniqueId) { PlayerState() }
}
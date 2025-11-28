package net.ririfa.bulletinboard.translation

import net.kyori.adventure.text.TextComponent
import net.ririfa.langman.MessageKey

sealed class BBMessageKey : MessageKey<BBMSGProvider, TextComponent> {
    sealed class GUI : BBMessageKey() {
        object Title : GUI() // BulletinBoard %version%

        sealed class Buttons : GUI() {
            sealed class Pagination : Buttons() {
                object Previous : GUI() // 前のページ
                object Next : GUI() // 次のページ
            }
        }

        sealed class Editor : GUI() {
            object NoTitle : GUI() // タイトルがありません...
            object NoContent : GUI() // コンテンツがありません...

            object Save : GUI() // 投稿を保存
            object Cancel : GUI() // 投稿をキャンセル
        }

        sealed class Other : GUI() {
            object NoPosts : GUI() // 投稿がありません
        }
    }

    sealed class Messages : BBMessageKey() {
        object NotPreviewing : Messages() // プレビュー中ではありません。
        object PostDraftNull : Messages() // 投稿の下書きが存在しません。

        sealed class Edit : Messages() {
            object EnterTitle : Messages() // 投稿のタイトルを入力してください:
            object EnterContent : Messages() // 投稿の内容を入力してください:
        }
    }

    sealed class Command : BBMessageKey() {
        sealed class Other : Command() {
            object PlayerOnly : Other() // このコマンドはプレイヤーのみ使用できます。
            object UnknownCommand : Other() // 不明なコマンドです。ヘルプを表示するには /%command% を入力してください。
        }
    }
}
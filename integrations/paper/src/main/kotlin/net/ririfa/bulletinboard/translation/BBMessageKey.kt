package net.ririfa.bulletinboard.translation

import net.kyori.adventure.text.TextComponent
import net.ririfa.langman.MessageKey

sealed class BBMessageKey : MessageKey<BBMSGProvider, TextComponent> {
    sealed class GUI : BBMessageKey() {
        object Title : GUI() // BulletinBoard %version%

        sealed class Buttons : GUI() {
            sealed class MainBoard : Buttons() {
                object NewPost : MainBoard() // 新しい投稿
                object MyPosts : MainBoard() // マイ投稿
                object AllPosts : MainBoard() // すべての投稿
                object DeletedPosts : MainBoard() // 削除済み投稿
                object AboutPlugin : MainBoard() // プラグインについて
                object Settings : MainBoard() // 設定
                object Help : MainBoard() // ヘルプ
                object Discord : MainBoard() // Discord
            }

            sealed class Pagination : Buttons() {
                object Previous : Pagination() // 前のページ
                object Next : Pagination() // 次のページ
            }
        }

        sealed class Editor : GUI() {
            object NoTitle : Editor() // タイトルがありません...
            object NoContent : Editor() // コンテンツがありません...

            object Save : Editor() // 投稿を保存
            object Cancel : Editor() // 投稿をキャンセル
        }

        sealed class Other : GUI() {
            object NoPosts : Other() // 投稿がありません

            object UnknownPlayer : Other() // 不明なプレイヤー
        }
    }

    sealed class Messages : BBMessageKey() {
        object NotPreviewing : Messages() // プレビュー中ではありません。
        object PostDraftNull : Messages() // 投稿の下書きが存在しません。

        object JoinDiscord : Messages() // Discordに参加して、コミュニティとつながりましょう！リンク: %link%

        sealed class Edit : Messages() {
            object EnterTitle : Edit() // 投稿のタイトルを入力してください:
            object EnterContent : Edit() // 投稿の内容を入力してください:
        }
    }

    sealed class Command : BBMessageKey() {
        sealed class DisplayPost : Command() {
            object AuthorLabel : DisplayPost() // 投稿者:
            object TitleLabel : DisplayPost() // タイトル:
            object ContentLabel : DisplayPost() // 内容:
            object Anonymous : DisplayPost() // 匿名
            object DateLabel : DisplayPost() // 日付:
        }

        sealed class Other : Command() {
            object PlayerOnly : Other() // このコマンドはプレイヤーのみ使用できます。
            object UnknownCommand : Other() // 不明なコマンドです。ヘルプを表示するには /%command% を入力してください。
        }
    }
}
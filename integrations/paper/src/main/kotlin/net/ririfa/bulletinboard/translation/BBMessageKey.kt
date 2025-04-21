package net.ririfa.bulletinboard.translation

import net.kyori.adventure.text.TextComponent
import net.ririfa.langman.MessageKey

sealed class BBMessageKey : MessageKey<BBMSGProvider, TextComponent> {
    sealed class GUI : BBMessageKey() {
        object Title : GUI()

        sealed class Buttons : GUI() {
            object BackButton : Buttons()

            sealed class MainBoard : Buttons() {
                object NewPost : MainBoard()
                object AllPosts : MainBoard()
                object MyPosts : MainBoard()
                object DeletedPosts : MainBoard()
                object AboutPlugin : MainBoard()
                object Settings : MainBoard()
                object Help : MainBoard()
            }

            sealed class MyPosts : Buttons() {
                object EditPost : MyPosts()
                object DeletePost : MyPosts()
            }

            sealed class AllPosts : Buttons() {
                object DeleteOthers : BBMessageKey()
            }

            sealed class DeletedPosts : Buttons() {
                object RestorePost : DeletedPosts()
                object DeletePostPermanently : DeletedPosts()
            }

            sealed class Paged : Buttons() {
                object Next : Paged()
                object Previous : Paged()
            }
        }

        sealed class Messages : GUI() {
            object EnterTitle : Messages()
            object EnterContent : Messages()

            object EnterTitleEdit : Messages()
            object EnterContentEdit : Messages()

            object PostSaved : Messages()

            object NotPreviewing : Messages()
        }

        sealed class Editor : GUI() {
            object NoTitle : Editor()
            object NoContent : Editor()
            object Save : Editor()
            object Cancel : Editor()
            object SaveEdit : Editor()
            object CancelEdit : Editor()

            object EditError : Editor()

            sealed class EditErrorReason : Editor() {
                object DraftNull : EditErrorReason()
            }
        }

        sealed class Other : GUI() {
            object NoPosts : Other()
            object UnknownPlayer : Other()
        }
    }

    sealed class Command : BBMessageKey() {
        sealed class DisplayPost : Command() {
            object TitleLabel : DisplayPost()
            object ContentLabel : DisplayPost()
            object AuthorLabel : DisplayPost()
            object DateLabel : DisplayPost()
            object Anonymous : DisplayPost()
        }

        sealed class Help : Command() {
            object HelpHeader : Help()

            object OpenBoard : Help()
            object NewPost : Help()
            object MyPosts : Help()
            object AllPosts : Help()
            object Settings : Help()
            object DeletedPosts : Help()
            object PreviewClose : Help()
        }

        sealed class Other : Command() {
            object PlayerOnly : Other()
            object UnknownCommand : Other()
        }
    }
}
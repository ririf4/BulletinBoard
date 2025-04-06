package net.ririfa.bulletinboard.translation

import net.kyori.adventure.text.TextComponent
import net.ririfa.langman.MessageKey

sealed class BBMessageKey : MessageKey<BBMSGProvider, TextComponent> {
    sealed class GUI : BBMessageKey() {
        object Title : GUI()

        sealed class Buttons : GUI() {
            sealed class MainBoard : Buttons() {
                object NewPost : MainBoard()
                object AllPosts : MainBoard()
                object MyPosts : MainBoard()
                object DeletedPosts : MainBoard()
                object AboutPlugin : MainBoard()
                object Settings : MainBoard()
                object Help : MainBoard()
            }

            sealed class Paged : Buttons() {
                object Next : Paged()
                object Previous : Paged()
            }
        }

        sealed class Other : GUI() {
            object NoPosts : Other()
           object UnknownPlayer : Other()
        }
    }

    sealed class DisplayPost : BBMessageKey() {
        object TitleLabel : DisplayPost()
        object ContentLabel : DisplayPost()
        object AuthorLabel : DisplayPost()
        object DateLabel : DisplayPost()
        object Anonymous : DisplayPost()
    }
}
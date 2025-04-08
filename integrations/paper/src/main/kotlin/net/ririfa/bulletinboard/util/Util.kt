package net.ririfa.bulletinboard.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.ririfa.bulletinboard.BExecutor
import net.ririfa.bulletinboard.Plugin
import net.ririfa.bulletinboard.translation.BBMessageKey
import net.ririfa.bulletinboard.translation.adapt
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun Player.playSoundMaster(sound: Sound, volume: Float = 1.0f, pitch: Float = 1.0f) {
    this.playSound(this.location, sound, volume, pitch)
}

fun getPlayerTimeZone(player: Player): TimeZone {
    val country = player.locale().country.uppercase()
    val timeZoneId = countryTimeZones[country] ?: "UTC"
    return TimeZone.getTimeZone(timeZoneId)
}

private val countryTimeZones = mapOf(
    "US" to "America/New_York", // アメリカ
    "GB" to "Europe/London",    // イギリス
    "JP" to "Asia/Tokyo",       // 日本
    "AU" to "Australia/Sydney", // オーストラリア
    "DE" to "Europe/Berlin",    // ドイツ
    "FR" to "Europe/Paris",     // フランス
    "CA" to "America/Toronto",  // カナダ
    "CN" to "Asia/Shanghai",    // 中国
    "IN" to "Asia/Kolkata",     // インド
    "BR" to "America/Sao_Paulo",// ブラジル
    "ZA" to "Africa/Johannesburg", // 南アフリカ
    "NZ" to "Pacific/Auckland"  // ニュージーランド
)

fun displayPost(player: Player, post: Post?) {
    if (post == null) return
    val cp = player.adapt()
    val playerTimeZone = getPlayerTimeZone(player)
    // Date in Result can never be null
    val zonedDateTime = ZonedDateTime.ofInstant(post.date.toInstant(), playerTimeZone.toZoneId())
    val authorName =
        // First try. Get from online player
        Bukkit.getPlayer(post.author)?.name
        // Second try. Get from offline player
            ?: Bukkit.getOfflinePlayer(post.author).name
            // If a player is not found, display "Unknown Player"
            ?: cp.getMessage(BBMessageKey.GUI.Other.UnknownPlayer)

    val authorComponent = if (!post.isAnonymous) {
        val ac = mapOf(
            "author" to authorName
        )
        cp.getMessage(BBMessageKey.Command.DisplayPost.AuthorLabel, ac)
    } else {
        val ac = mapOf(
            "author" to cp.getMessage(BBMessageKey.Command.DisplayPost.Anonymous).content()
        )
        cp.getMessage(BBMessageKey.Command.DisplayPost.AuthorLabel, ac)
    }

    val plainTitle = PlainTextComponentSerializer.plainText().serialize(post.title)
    val plainContent = PlainTextComponentSerializer.plainText().serialize(post.content)

    val titleAc = mapOf(
        "title" to plainTitle
    )
    val titleComponent = cp.getMessage(BBMessageKey.Command.DisplayPost.TitleLabel, titleAc)

    val contentAc = mapOf(
        "content" to plainContent
    )
    val contentComponent = cp.getMessage(BBMessageKey.Command.DisplayPost.ContentLabel, contentAc)

    val dateAc = mapOf(
        "date" to zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).toString()
    )
    val dateComponent = cp.getMessage(BBMessageKey.Command.DisplayPost.DateLabel, dateAc)

    BExecutor.execute {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)

        val message = Component.text("---------------------------------", NamedTextColor.DARK_GRAY)
            .append(Component.newline())
            .append(titleComponent.color(NamedTextColor.WHITE))
            .append(Component.newline())
            .append(contentComponent.color(NamedTextColor.WHITE))
            .append(Component.newline())
            .append(authorComponent.color(NamedTextColor.WHITE))
            .append(Component.newline())
            .append(dateComponent.color(NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("---------------------------------", NamedTextColor.DARK_GRAY))


        player.sendMessage(message)
    }
}

fun displayAbout(player: Player) {
    player.playSoundMaster(Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 2.0f)

    val header = Component.text("=== BulletinBoard ===")
        .color(NamedTextColor.DARK_GREEN)
        .decorate(TextDecoration.BOLD)

    val versionMessage = Component.text("Version: v${Plugin.version}")
        .color(NamedTextColor.GOLD)
        .decorate(TextDecoration.BOLD)

    val authorMessage = Component.text("Made by ${Plugin.authors.joinToString(", ")}")
        .color(NamedTextColor.BLUE)
        .decorate(TextDecoration.ITALIC)

    val description = Component.text(Plugin.pluginDes ?: "No Description")
        .color(NamedTextColor.WHITE)

    player.sendMessage(header)
    player.sendMessage(versionMessage)
    player.sendMessage(authorMessage)
    player.sendMessage(description)
    player.sendMessage(
        Component.text("===================")
            .color(NamedTextColor.DARK_GREEN)
            .decorate(TextDecoration.BOLD)
    )
}

fun displayHelp(player: Player) {
    val p = player.adapt()
    val headerComponent = p.getMessage(BBMessageKey.Command.Help.HelpHeader)
        .color(NamedTextColor.GOLD)
        .decorate(TextDecoration.BOLD)

    val hStartComponent = Component.text("=======").color(NamedTextColor.GOLD)
    val hEndComponent = Component.text("=======").color(NamedTextColor.GOLD)

    val commandsDescription = listOf(
        "openboard" to BBMessageKey.Command.Help.OpenBoard,
        "newpost" to BBMessageKey.Command.Help.NewPost,
        "myposts" to BBMessageKey.Command.Help.MyPosts,
        "posts" to BBMessageKey.Command.Help.AllPosts,
        "settings" to BBMessageKey.Command.Help.Settings,
        "deletedposts" to BBMessageKey.Command.Help.DeletedPosts,
        "previewclose" to BBMessageKey.Command.Help.PreviewClose
    )

    player.sendMessage(hStartComponent.append(headerComponent).append(hEndComponent))

    commandsDescription.forEach { (command, key) ->
        player.sendMessage(
            Component.text("$command - ").append(p.getMessage(key))
                .color(NamedTextColor.GREEN)
        )
    }
    player.sendMessage(Component.text("=======================").color(NamedTextColor.GOLD))
}
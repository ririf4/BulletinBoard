@file:Suppress("DuplicatedCode")

package net.ririfa.bulletinboard

import dev.swiftstorm.akkaradb.engine.AkkDSL
import dev.swiftstorm.akkaradb.engine.Id
import dev.swiftstorm.akkaradb.engine.PackedTable
import dev.swiftstorm.akkaradb.engine.StartupMode
import dev.swiftstorm.akkaradb.format.akk.parity.RSParityCoder
import net.kyori.adventure.text.TextComponent
import net.ririfa.bulletinboard.util.ShortUUID
import org.bukkit.entity.Player
import java.util.*

object DataBase {
    var postsInitialized = false
    var settingsInitialized = false

    val posts: PackedTable<Post, ShortUUID> by lazy {
        Logger.info("Initializing Database...")
        postsInitialized = true
		AkkDSL.open(DBDir.resolve("posts"), StartupMode.ULTRA_FAST) {
			m = 2; parityCoder = RSParityCoder(2)
		}
	}

    val playerSettings: PackedTable<PlayerSettings, UUID> by lazy {
        Logger.info("Initializing Database...")
        settingsInitialized = true
		AkkDSL.open(DBDir.resolve("settings"), StartupMode.ULTRA_FAST) {
			m = 2; parityCoder = RSParityCoder(2)
		}
	}

	fun insertPost(post: Post) {
		posts.put(post)
	}

	fun getMyPosts(player: Player): List<Post> {
		return posts.runToList {
			author == player.uniqueId && !isDeleted
		}
	}

	fun getAllPosts(): List<Post> {
        println("Getting all posts...")
        return posts.runToList { !isDeleted }
	}

	fun updatePost(post: Post) = insertPost(post)

	fun getDeletedPosts(player: Player): List<Post> {
		return posts.runToList {
			author == player.uniqueId && isDeleted
		}
	}

    fun close() {
        Logger.info("Closing databases...")

        if (postsInitialized) {
            Logger.info("Closing posts...")
            posts.close()
        }
        if (settingsInitialized) {
            Logger.info("Closing settings...")
            playerSettings.close()
        }

        Logger.info("Database shutdown complete")
    }

	data class Post(
        @Id val id: ShortUUID,
        val author: UUID,
        val title: TextComponent,
        val content: TextComponent,
        val isAnonymous: Boolean,
        val date: Date,
        val isDeleted: Boolean,
	)

	//TODO: Manage with enum?
	data class PlayerSettings(
        @Id val uuid: UUID,
        val settingKey: String,
        val settingValue: String
	)
}

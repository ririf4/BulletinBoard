@file:Suppress("DuplicatedCode")

package net.ririfa.bulletinboard

import dev.swiftstorm.akkaradb.engine.AkkDSL
import dev.swiftstorm.akkaradb.engine.Id
import dev.swiftstorm.akkaradb.engine.PackedTable
import dev.swiftstorm.akkaradb.engine.StartupMode
import dev.swiftstorm.akkaradb.format.akk.parity.RSParityCoder
import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.util.PostDraft
import net.ririfa.bulletinboard.util.ShortUUID
import org.bukkit.entity.Player
import java.util.*

object DataBase {
    var postsInitialized = false
    var settingsInitialized = false

    val posts: PackedTable<Post, ShortUUID> by lazy {
        postsInitialized = true
		AkkDSL.open(DBDir.resolve("posts"), StartupMode.ULTRA_FAST) {
			m = 2; parityCoder = RSParityCoder(2); debug = true
		}
	}

    val playerSettings: PackedTable<PlayerSettings, UUID> by lazy {
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
        return posts.runToList { !isDeleted }
	}

	fun updatePost(post: Post) = insertPost(post)

	fun getDeletedPosts(player: Player): List<Post> {
		return posts.runToList {
			author == player.uniqueId && isDeleted
		}
	}

    fun getDeletedPost(): List<Post> {
        return posts.runToList { isDeleted }
    }

    fun deletePost(id: ShortUUID) {
        val post = posts.get(id)
        post?.copy(isDeleted = true).also {
            it?.let { entity -> posts.put(entity) }
        }
    }

    fun deletePermanently(id: ShortUUID) {
        posts.delete(id)
    }

    fun close() {
        if (postsInitialized) {
            posts.close()
        }
        if (settingsInitialized) {
            playerSettings.close()
        }
    }

	data class Post(
        @Id val id: ShortUUID,
        val author: UUID,
        val title: Component,
        val content: Component,
        val isAnonymous: Boolean,
        val date: Date,
        val editedOn: Date,
        val isDeleted: Boolean,
	)

	//TODO: Manage with enum?
	data class PlayerSettings(
        @Id val uuid: UUID,
        val settingKey: String,
        val settingValue: String
	)
}

fun DataBase.Post.toDraft(): PostDraft = PostDraft(
    id = this.id,
    title = this.title,
    content = this.content,
    isAnonymous = this.isAnonymous,
    date = this.date
)
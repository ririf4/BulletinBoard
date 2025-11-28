@file:Suppress("DuplicatedCode")

package net.ririfa.bulletinboard

import dev.swiftstorm.akkaradb.engine.AkkDSL
import dev.swiftstorm.akkaradb.engine.PackedTable
import dev.swiftstorm.akkaradb.engine.StartupMode
import dev.swiftstorm.akkaradb.format.akk.parity.RSParityCoder
import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.util.ShortUUID
import org.bukkit.entity.Player
import java.util.*

object DataBase {
	val posts: PackedTable<Post> by lazy {
		AkkDSL.open(DBDir.resolve("posts"), StartupMode.ULTRA_FAST) {
			m = 2; parityCoder = RSParityCoder(2)
		}
	}

	val playerSettings: PackedTable<PlayerSettings> by lazy {
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
		return posts.runToList {
			!isDeleted
		}
	}

	fun updatePost(post: Post) = insertPost(post)

	fun getDeletedPosts(player: Player): List<Post> {
		return posts.runToList {
			author == player.uniqueId && isDeleted
		}
	}

	data class Post(
		val id: ShortUUID,
		val author: UUID,
		val title: Component,
		val content: Component,
		val isAnonymous: Boolean,
		val date: Date,
		val isDeleted: Boolean,
	)

	//TODO: Manage with enum?
	data class PlayerSettings(
		val uuid: UUID,
		val settingKey: String,
		val settingValue: String
	)
}

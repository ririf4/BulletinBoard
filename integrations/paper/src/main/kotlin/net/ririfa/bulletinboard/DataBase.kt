@file:Suppress("DuplicatedCode")

package net.ririfa.bulletinboard

import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.BulletinBoard.Companion.logger
import net.ririfa.bulletinboard.util.Post
import net.ririfa.bulletinboard.util.ShortUUID
import org.apache.commons.lang3.time.DateUtils.parseDate
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class DataBase(private val plugin: BulletinBoard) {
	private var connection: Connection? = null
		get() {
			if (field == null || field!!.isClosed) {
				throw SQLException("Connection is not open.")
			}
			return field
		}
	private var memoryConnection: Connection? = null
		get() {
			if (field == null || field!!.isClosed) {
				throw SQLException("Connection is not open.")
			}
			return field
		}

	fun start(): Boolean {
		return try {
			val fileDbUrl = "jdbc:h2:${plugin.dataFolder}/database.db;MODE=SQLite;AUTO_SERVER=TRUE"
			val memoryDbUrl = "jdbc:h2:mem:bulletinboard;MODE=SQLite;DB_CLOSE_DELAY=-1"

			connection = DriverManager.getConnection(fileDbUrl, "sa", "")
			memoryConnection = DriverManager.getConnection(memoryDbUrl, "sa", "")

			logger.info("H2 (Persistent + In-Memory) started successfully!")
			true
		} catch (e: SQLException) {
			logger.error("Failed to start H2 database: ${e.message}")
			false
		}
	}

	fun stop() {
		try {
			memoryConnection?.close()
			connection?.close()
			logger.info("H2 database connections closed.")
		} catch (e: SQLException) {
			logger.error("Failed to close H2 database: ${e.message}")
		}
	}

	object Accessor {
		fun getAllPosts(): List<Post> = transaction {
			Tables.Posts
				.selectAll()
				.where { Tables.Posts.isDeleted eq false }
				.map(::toPost)
		}

		fun getMyPosts(player: Player): List<Post> = transaction {
			Tables.Posts
				.selectAll()
				.where {
					(Tables.Posts.author eq player.uniqueId.toString()) and (Tables.Posts.isDeleted eq false)
				}
				.map(::toPost)
		}

		fun getDeletedPosts(player: Player): List<Post> = transaction {
			Tables.Posts
				.selectAll()
				.where {
					(Tables.Posts.author eq player.uniqueId.toString()) and (Tables.Posts.isDeleted eq true)
				}
				.map(::toPost)
		}

		internal fun toPost(row: ResultRow): Post = Post(
			id = ShortUUID.fromShortString(row[Tables.Posts.id]),
			author = UUID.fromString(row[Tables.Posts.author]),
			title = Component.text(row[Tables.Posts.title]),
			content = Component.text(row[Tables.Posts.content]),
			isAnonymous = row[Tables.Posts.isAnonymous],
			date = parseDate(row[Tables.Posts.date]),
			isDeleted = row[Tables.Posts.isDeleted],
		)
	}

	fun createRequiredTables() = transaction {
		SchemaUtils.create(
			Tables.Posts,
			Tables.PlayerSettings
		)
		logger.info("All required tables created!")
	}


	object Tables {
		object Posts : Table("posts") {
			val id = text("id")
			val author = text("author")
			val title = text("title")
			val content = text("content")
			val isAnonymous = bool("isAnonymous").default(false)
			val date = text("date")
			val isDeleted = bool("isDeleted").default(false)

			override val primaryKey = PrimaryKey(id)
		}

		object PlayerSettings : Table("playerSettings") {
			val uuid = text("uuid")
			val settingKey = text("settingKey")
			val settingValue = text("settingValue")

			override val primaryKey = PrimaryKey(uuid, settingKey)
		}
	}
}

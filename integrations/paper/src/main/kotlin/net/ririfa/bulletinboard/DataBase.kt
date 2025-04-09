@file:Suppress("DuplicatedCode")

package net.ririfa.bulletinboard

import net.kyori.adventure.text.Component
import net.ririfa.beacon.IEventHandler
import net.ririfa.bulletinboard.BulletinBoard.Companion.logger
import net.ririfa.bulletinboard.util.*
import org.apache.commons.lang3.time.DateUtils.parseDate
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DataBase(private val plugin: BulletinBoard) : IEventHandler {
	lateinit var db: Database
	lateinit var memDb: Database

	fun start(): Boolean {
		return try {
			val fileDbUrl = "jdbc:h2:${plugin.dataFolder}/database.db;MODE=SQLite;AUTO_SERVER=TRUE"
			val memoryDbUrl = "jdbc:h2:mem:bulletinboard;MODE=SQLite;DB_CLOSE_DELAY=-1"

			db = Database.connect(fileDbUrl, driver = "org.h2.Driver", user = "sa", password = "")
			memDb = Database.connect(memoryDbUrl, driver = "org.h2.Driver", user = "sa", password = "")

			logger.info("H2 (Persistent + In-Memory) started successfully!")
			true
		} catch (e: Exception) {
			logger.error("Failed to start H2 database: ${e.message}")
			false
		}
	}

	@Suppress("RemoveRedundantQualifierName")
	object Accessor {
		fun getAllPosts(): List<Post> = DB {
			Tables.Posts
				.selectAll()
				.where { Tables.Posts.isDeleted eq false }
				.map(::toPost)
		}

		fun getMyPosts(player: Player): List<Post> = DB {
			Tables.Posts
				.selectAll()
				.where {
					(Tables.Posts.author eq player.uniqueId.toString()) and (Tables.Posts.isDeleted eq false)
				}
				.map(::toPost)
		}

		fun getDeletedPosts(player: Player): List<Post> = DB {
			Tables.Posts
				.selectAll()
				.where {
					(Tables.Posts.author eq player.uniqueId.toString()) and (Tables.Posts.isDeleted eq true)
				}
				.map(::toPost)
		}

		fun insertPost(post: Post) {
			DB {
				Tables.Posts.insert {
					it[id] = post.id.toShortString()
					it[author] = post.author.toString()
					it[title] = post.title.content()
					it[content] = post.content.content()
					it[isAnonymous] = post.isAnonymous
					it[date] = DateFormatUtil.format(post.date)
					it[isDeleted] = post.isDeleted
				}
			}
			DiffTracker.markChanged(Tables.Posts)
		}

		fun updatePost(post: Post) {
			DB {
				Tables.Posts.update({ Tables.Posts.id eq post.id.toShortString() }) {
					it[title] = post.title.content()
					it[content] = post.content.content()
					it[isAnonymous] = post.isAnonymous
					it[date] = DateFormatUtil.format(post.date)
					it[isDeleted] = post.isDeleted
				}
			}
			DiffTracker.markChanged(Tables.Posts)
		}

		fun deletePost(post: Post) {
			DB {
				Tables.Posts
					.update({ Tables.Posts.id eq post.id.toShortString() }) {
						it[isDeleted] = true
					}
			}
			DiffTracker.markChanged(Tables.Posts)
		}

		fun deletePostPermanently(post: Post) {
			DB {

				Tables.Posts
					.deleteWhere { Tables.Posts.id eq post.id.toShortString() }
			}
			DiffTracker.markChanged(Tables.Posts)
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

	override fun initHandlers() {
//		handler<DBTransactionEvent> { event ->
//
//		}
	}


	@Suppress("ExposedReference")
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

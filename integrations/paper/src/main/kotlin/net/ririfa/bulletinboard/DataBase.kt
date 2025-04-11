@file:Suppress("DuplicatedCode")

package net.ririfa.bulletinboard

import net.kyori.adventure.text.Component
import net.ririfa.bulletinboard.BulletinBoard.Companion.logger
import net.ririfa.bulletinboard.util.*
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.io.File
import java.util.*

class DataBase(private val plugin: BulletinBoard) {
	lateinit var db: Database
	lateinit var memDb: Database

	fun start(): Boolean {
		return try {
			// database.mv.db
			val dbFile = File(plugin.dataFolder, "database").absoluteFile.path
			val fileDbUrl = "jdbc:h2:$dbFile;AUTO_SERVER=TRUE"
			val memoryDbUrl = "jdbc:h2:mem:bulletinboard;DB_CLOSE_DELAY=-1"

			db = Database.connect(fileDbUrl, driver = "org.h2.Driver", user = "sa", password = "")
			memDb = Database.connect(memoryDbUrl, driver = "org.h2.Driver", user = "sa", password = "")

			createRequiredTables()

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
			DiffTracker.markChanged(
				Tables.Posts,
				post.id.toShortString(),
				Tables.Posts.title,
				Tables.Posts.content,
				Tables.Posts.isAnonymous,
				Tables.Posts.date,
				Tables.Posts.isDeleted
			)
		}

		fun updatePost(post: Post) {
			val id = post.id.toShortString()

			val original = DB {
				Tables.Posts.selectAll()
					.where { Tables.Posts.id eq id }.firstOrNull()
			} ?: return

			val changedColumns = mutableListOf<Column<*>>()

			DB {
				Tables.Posts.update({ Tables.Posts.id eq id }) {
					if (post.title.content() != original[Tables.Posts.title]) {
						it[title] = post.title.content()
						changedColumns += Tables.Posts.title
					}
					if (post.content.content() != original[Tables.Posts.content]) {
						it[content] = post.content.content()
						changedColumns += Tables.Posts.content
					}
					if (post.isAnonymous != original[Tables.Posts.isAnonymous]) {
						it[isAnonymous] = post.isAnonymous
						changedColumns += Tables.Posts.isAnonymous
					}
					if (DateFormatUtil.format(post.date) != original[Tables.Posts.date]) {
						it[date] = DateFormatUtil.format(post.date)
						changedColumns += Tables.Posts.date
					}
					if (post.isDeleted != original[Tables.Posts.isDeleted]) {
						it[isDeleted] = post.isDeleted
						changedColumns += Tables.Posts.isDeleted
					}
				}
			}

			if (changedColumns.isNotEmpty()) {
				DiffTracker.markChanged(Tables.Posts, id, *changedColumns.toTypedArray())
			}
		}

		fun deletePost(post: Post) {
			DB {
				Tables.Posts.update({ Tables.Posts.id eq post.id.toShortString() }) {
					it[isDeleted] = true
				}
			}
			DiffTracker.markChanged(
				Tables.Posts,
				post.id.toShortString(),
				Tables.Posts.isDeleted
			)
		}

		fun deletePostPermanently(post: Post) {
			DB {
				Tables.Posts.deleteWhere { Tables.Posts.id eq post.id.toShortString() }
			}
			DiffTracker.markDeleted(Tables.Posts, post.id.toShortString())
		}

		internal fun toPost(row: ResultRow): Post = Post(
			id = ShortUUID.fromShortString(row[Tables.Posts.id]),
			author = UUID.fromString(row[Tables.Posts.author]),
			title = Component.text(row[Tables.Posts.title]),
			content = Component.text(row[Tables.Posts.content]),
			isAnonymous = row[Tables.Posts.isAnonymous],
			date = DateFormatUtil.parse(row[Tables.Posts.date]),
			isDeleted = row[Tables.Posts.isDeleted],
		)
	}

	fun createRequiredTables() {
		DB(db) {
			SchemaUtils.create(
				Tables.Posts,
				Tables.PlayerSettings
			)
		}

		DB(memDb) {
			SchemaUtils.create(
				Tables.Posts,
				Tables.PlayerSettings
			)
		}

		logger.info("All required tables created on both databases!")
	}

	fun syncChangedColumns() {
		val changes = DiffTracker.consumeChanges()
		val deleted = DiffTracker.consumeDeleted()
		if (changes.isEmpty() && deleted.isEmpty()) return

		DB(db) {
			changes.forEach { (tableName, rows) ->
				val table = Tables::class.nestedClasses
					.mapNotNull { it.objectInstance as? Table }
					.firstOrNull { it.tableName == tableName } ?: run {
					logger.warn("Unknown table: $tableName")
					return@forEach
				}

				val idColumn = table.columns.firstOrNull { it.name == "id" } as? Column<String> ?: run {
					logger.warn("No id column found in $tableName")
					return@forEach
				}

				rows.forEach { (rowId, columns) ->
					val memRow = DB(memDb) {
						table.selectAll()
							.where { idColumn eq rowId }
							.limit(1)
							.firstOrNull()
					} ?: return@forEach

					val exists = table.selectAll()
						.where { idColumn eq rowId }
						.limit(1)
						.count() > 0

					if (exists) {
						table.update({ idColumn eq rowId }) {
							columns.forEach { colName ->
								val col = table.columns.firstOrNull { it.name == colName } ?: return@forEach

								@Suppress("UNCHECKED_CAST")
								val value = memRow[col as Column<Any?>]
								it[col] = value
							}
						}
					} else {
						table.insert {
							table.columns.forEach { col ->
								@Suppress("UNCHECKED_CAST")
								val value = memRow[col as Column<Any?>]
								it[col] = value
							}
						}
					}
				}
			}

			deleted.forEach { (tableName, ids) ->
				val table = Tables::class.nestedClasses
					.mapNotNull { it.objectInstance as? Table }
					.firstOrNull { it.tableName == tableName } ?: run {
					logger.warn("Unknown table (delete): $tableName")
					return@forEach
				}

				val idCol = table.columns.firstOrNull { it.name == "id" } as? Column<String> ?: run {
					logger.warn("No id column found in $tableName for delete")
					return@forEach
				}

				table.deleteWhere {
					idCol inList ids
				}
			}
		}

		logger.info("Synced changed columns for: ${changes.keys + deleted.keys}")
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

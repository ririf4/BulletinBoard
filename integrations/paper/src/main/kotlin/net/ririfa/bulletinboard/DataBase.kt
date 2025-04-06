package net.ririfa.bulletinboard

import net.ririfa.bulletinboard.BulletinBoard.Companion.logger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

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

	//TODO
	fun createRequiredTables() {

	}

	object Tables {

	}
}

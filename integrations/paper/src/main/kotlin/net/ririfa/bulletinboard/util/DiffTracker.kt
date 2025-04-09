package net.ririfa.bulletinboard.util

import org.jetbrains.exposed.sql.Table

object DiffTracker {
    private val modifiedTables = mutableSetOf<String>()

    fun markChanged(table: Table) {
        modifiedTables += table.tableName
    }

    fun consumeChanges(): Set<String> {
        val changes = modifiedTables.toSet()
        modifiedTables.clear()
        return changes
    }
}

package net.ririfa.bulletinboard.util

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object DiffTracker {
    private val deletedRows = mutableMapOf<String, MutableSet<String>>()
    private val modifiedData = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()
    // Map<TableName, Map<RowID, Set<ColumnName>>>

    fun markChanged(table: Table, rowId: String, column: Column<*>) {
        val tableMap = modifiedData.getOrPut(table.tableName) { mutableMapOf() }
        val columns = tableMap.getOrPut(rowId) { mutableSetOf() }
        columns += column.name
    }

    fun markChanged(table: Table, rowId: String, vararg columns: Column<*>) {
        columns.forEach { markChanged(table, rowId, it) }
    }

    fun markDeleted(table: Table, rowId: String) {
        val set = deletedRows.getOrPut(table.tableName) { mutableSetOf() }
        set += rowId
    }

    fun consumeChanges(): Map<String, Map<String, Set<String>>> {
        val result = modifiedData.mapValues { it.value.mapValues { entry -> entry.value.toSet() } }
        modifiedData.clear()
        return result
    }

    fun consumeDeleted(): Map<String, Set<String>> {
        val copy = deletedRows.mapValues { it.value.toSet() }
        deletedRows.clear()
        return copy
    }
}

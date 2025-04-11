@file:Suppress("FunctionName")

package net.ririfa.bulletinboard.util

import net.ririfa.bulletinboard.DB
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> DB(block: () -> T): T {
    return transaction(DB.memDb) { block() }
}

fun <T> DB(database: Database, block: () -> T): T {
    return transaction(database) { block() }
}
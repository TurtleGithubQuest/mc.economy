package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.database
import dev.turtle.economy.database.LogsColumn
import dev.turtle.economy.database.LogsTables
import dev.turtle.economy.database.OrderBy
import dev.turtle.turtlelib.command.TurtleCommand
import dev.turtle.turtlelib.command.TurtleSubCommand

class Logs(turtleCommand: TurtleCommand): TurtleSubCommand("logs", turtleCommand) {
    init { //todo: Add filters
        ArgumentData("table", LogsTables.entries, String::class, defaultValue = LogsTables.Balance,isRequired = false)
        ArgumentData("orderColumn", LogsColumn.entries, String::class, defaultValue = LogsColumn.Unix, isRequired = false)
        ArgumentData("orderBy", OrderBy.entries, String::class, defaultValue = OrderBy.DESC ,isRequired = false)
        ArgumentData("limit", listOf(5, 10, 15, 25), Int::class, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val table = getValue("table")?.let {
            try { LogsTables.valueOf(it.toString().uppercase()) } catch (_: IllegalArgumentException) { LogsTables.Balance }
        } ?: return true
        val orderBy = getValue("orderBy", defaultValue="DESC")?.let {
            try { OrderBy.valueOf(it.toString().uppercase()) } catch (_: IllegalArgumentException) { OrderBy.DESC }
        }?: return true
        val orderColumn = getValue("orderColumn", defaultValue= LogsColumn.Unix)?.let {
            try { LogsColumn.valueOf(it.toString()) } catch (_: IllegalArgumentException) { LogsColumn.Unix }
        }?: return true
        val limit = getValue("limit", defaultValue=15)
            ?.toString()?.toInt()?.coerceAtMost(50)
            ?: return true
        database.getLogs(table, orderColumn, orderBy, limit).forEach {
            it.send(cs!!)
        }
        return true
    }
    override fun onSuggestion(argumentData: ArgumentData) {}
}

package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.economy.database.Column
import dev.turtle.economy.database.OrderBy
import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand

class Balances(turtleCommand: TurtleCommand): TurtleSubCommand("balances", turtleCommand) {
    init {
        ArgumentData("currency", currencies.keys.toList(), String::class, isRequired = false)
        ArgumentData("orderColumn", Column.entries, String::class, defaultValue = Column.Balance, isRequired = false)
        ArgumentData("orderBy", OrderBy.entries, String::class, defaultValue = OrderBy.DESC ,isRequired = false)
        ArgumentData("limit", listOf(5, 10, 15, 25), Int::class, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val currencyName = getValue("currency", defaultValue=currencies[currencies.keys.first()]?.name)?.toString()?: return true
        currencies[currencyName]?: run {
            turtle.messageFactory.newMessage("command.turtleeconomy.currency-not-found").placeholder("currency", currencyName).fromConfig().send(cs!!)
            return true
        }
        val orderBy = getValue("orderBy", defaultValue="DESC")?.let {
            try { OrderBy.valueOf(it.toString().uppercase()) } catch (_: IllegalArgumentException) { OrderBy.DESC }
        }?: return true
        val orderColumn = getValue("orderColumn", defaultValue=Column.Balance)?.let {
            try { Column.valueOf(it.toString()) } catch (_: IllegalArgumentException) { Column.Balance }
        }?: return true
        val limit = getValue("limit", defaultValue=15)
            ?.toString()?.toInt()?.coerceAtMost(50)
            ?: return true
        val balances = database.getBalances(currencyName, orderColumn, orderBy, limit)
        turtle.messageFactory.newMessage("command.turtleeconomy.balances.balances-top").placeholders(hashMapOf("CURRENCY" to currencyName)).fromConfig().send(cs!!)
        balances.forEach {
            turtle.messageFactory.newMessage("command.turtleeconomy.balances.balances-entry").placeholders(
                hashMapOf(
                    "PLAYER" to it.nickname,
                    "BALANCE" to it.balance.toString()
                )
            ).fromConfig().send(cs!!)
        }
        turtle.messageFactory.newMessage("command.turtleeconomy.balances.balances-bottom")
            .placeholders(hashMapOf("CURRENCY" to currencyName))
            .fromConfig().send(cs!!)
        return true
    }

    override fun onSuggestion(argumentData: ArgumentData) {
    }
}
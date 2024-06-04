package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.economy.database.BalanceChange
import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand

class Wealth(turtleCommand: TurtleCommand): TurtleSubCommand("wealth", turtleCommand) {
    init {
        ArgumentData("player", null, String::class)
        ArgumentData("action", BalanceChange.entries, String::class)
        ArgumentData("currency", currencies.keys.toList(), String::class)
        ArgumentData("amount", listOf(1, 16, 32, 64), Int::class, defaultValue = 1, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val action = getValue("action")?.toString() ?: return true
        try {
            val balanceChange = BalanceChange.valueOf(action.uppercase())
            val targetName = getValue("player", cs!!.name)?.toString() ?: return true
            val currency = getValue("currency")?.let { currencies[it.toString()]?:return true } ?: return true
            val amount = getValue("amount")?.let { it as Int } ?: return true
            val placeholders: HashMap<String, Any> = hashMapOf(
                "AMOUNT" to amount.toString(),
                "SENDER" to cs!!.name,
                "CURRENCY" to currency.name,
                "CURRENCY_SYMBOL" to currency.symbol,
                "TARGET" to targetName,
                "ACTION" to action
            )
            database.getPlayer(targetName).updateBalance(balanceChange, currency.name, amount)
            turtle.server.getPlayer(targetName)?.let { target -> turtle.messageFactory.newMessage("command.turtleeconomy.wealth.balance-change.target.${action.lowercase()}").placeholders(placeholders).fromConfig().send(target) }
            turtle.messageFactory.newMessage("command.turtleeconomy.wealth.balance-change.sender").placeholders(placeholders).fromConfig().send(cs!!)

        } catch(_: IllegalArgumentException) {
            turtle.messageFactory.newMessage("command.turtleeconomy.wealth.action-not-found").placeholders(hashMapOf(
                "ACTIONS" to BalanceChange.entries,
                "ACTION" to action
            )).fromConfig().send(cs!!)
        }
        return true
    }
    override fun onSuggestion(argumentData: ArgumentData) {}
}
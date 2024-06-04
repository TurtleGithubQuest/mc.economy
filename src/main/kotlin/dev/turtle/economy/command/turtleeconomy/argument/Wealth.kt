package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.economy.database.BalanceChange
import dev.turtle.economy.database.Via
import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand
import org.bukkit.Bukkit

class Wealth(turtleCommand: TurtleCommand): TurtleSubCommand("wealth", turtleCommand) {
    init {
        ArgumentData("player", null, String::class)
        ArgumentData("action", BalanceChange.entries, String::class)
        ArgumentData("currency", currencies.keys.toList(), String::class)
        ArgumentData("amount", listOf(100, 1000, 2500, 5000), Number::class, defaultValue = 100, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val action = getValue("action")?.toString() ?: return true
        try {
            val balanceChange = BalanceChange.valueOf(action.uppercase())
            val targetName = getValue("player", cs!!.name)?.toString() ?: return true
            val currency = getValue("currency")?.let { currencies[it.toString()]?:return true } ?: return true
            val placeholders: MutableMap<String, Any> = mutableMapOf(
                "SENDER" to cs!!.name,
                "MIN" to currency.minimalValue,
                "DECIMALS" to currency.decimals,
                "CURRENCY" to currency.name,
                "SYMBOL" to currency.symbol,
                "TARGET" to targetName,
                "ACTION" to action
            )
            val amount = getValue("amount")?.let {
                val amount = currency.getAmountBigInt(it as Double)
                placeholders["AMOUNT"] = currency.getAmountForHuman(amount)
                if (currency.hasExcessiveDecimals(it)) {
                    turtle.messageFactory.newMessage("command.turtleeconomy.excessive-decimals")
                        .placeholders(HashMap(placeholders)).fromConfig().send(cs!!)
                    return true
                }
                amount
            } ?: return true
            database.getPlayer(targetName, (Bukkit.getPlayer(targetName)?:Bukkit.getOfflinePlayer(targetName)).uniqueId.toString()).updateBalance(balanceChange, currency.name, amount, cs!!.name, Via.CMD)
            turtle.server.getPlayer(targetName)?.let { target -> turtle.messageFactory.newMessage("command.turtleeconomy.wealth.balance-change.target.${action.lowercase()}").placeholders(HashMap(placeholders)).fromConfig().send(target) }
            turtle.messageFactory.newMessage("command.turtleeconomy.wealth.balance-change.sender").placeholders(HashMap(placeholders)).fromConfig().send(cs!!)

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
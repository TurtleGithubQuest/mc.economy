package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.economy.database.BalanceResult
import dev.turtle.economy.database.DbPlayer
import dev.turtle.economy.database.Via
import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand
import org.bukkit.Bukkit

class Transfer(turtleCommand: TurtleCommand): TurtleSubCommand("transfer", turtleCommand) {
    init {
        ArgumentData("player", null, String::class)
        ArgumentData("currency", currencies.keys.toList(), String::class)
        ArgumentData("amount", listOf(100, 1000, 2500, 5000), Number::class, defaultValue = 1, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val target = getValue("player")?.toString() ?: return true
        val currencyName = getValue("currency")?.toString() ?: return true
        val currency = getValue("currency")?.let { currencies[it.toString()]?:return true } ?: return true
        val amount = getValue("amount")?.let { it as Double } ?: return true
        val placeholders: HashMap<String, Any> = hashMapOf(
            "SENDER" to cs!!.name,
            "TARGET" to target,
            "CURRENCY" to currencyName,
            "SYMBOL" to currency.symbol,
            "AMOUNT" to amount,
            "CURRENCIES" to currencies.keys.joinToString(", ") as Any
        )
        val transferOutcome = DbPlayer(database, cs!!.name).transferAmount(target, currencyName, currency.getAmountBigInt(amount), cs!!.name, Via.CMD)
        if (transferOutcome.isOk()) {
            turtle.messageFactory.newMessage("command.turtleeconomy.transfer.sent").placeholders(placeholders)
                .fromConfig().send(cs!!)
            Bukkit.getPlayer(target)?.let {
                turtle.messageFactory.newMessage("command.turtleeconomy.transfer.received").placeholders(placeholders)
                    .fromConfig().send(it)
            }
        } else {
            turtle.messageFactory.newMessage("command.turtleeconomy.transfer.error.${(transferOutcome as BalanceResult.Error).error.toString().lowercase()}").placeholders(placeholders)
                .fromConfig().send(cs!!)
        }
        return true
    }

    override fun onSuggestion(argumentData: ArgumentData) {}
}
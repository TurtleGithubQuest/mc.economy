package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.turtlelib.command.TurtleCommand
import dev.turtle.turtlelib.command.TurtleSubCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Balance(turtleCommand: TurtleCommand): TurtleSubCommand("balance", turtleCommand) {
    init {
        ArgumentData("player", null, Player::class, isRequired = false)
        ArgumentData("currency", currencies.keys.toList(), String::class, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val target = getValue("player", defaultValue=cs!!.name)?.let { it as CommandSender }?: return true
        val currency = getValue("currency")?.let { currencies[it]?:return true } ?: return true
        val player = database.getPlayer(target.name)
        val balance = currency.getAmountForHuman(player.getBalance(currency.name))
        turtle.messageFactory.newMessage("command.turtleeconomy.balance.targets-balance").placeholders(
            HashMap(hashMapOf("TARGET" to target.name, "BALANCE" to balance)+currency.placeholderMap)
        ).fromConfig().send(cs!!)
        return true
    }

    override fun onSuggestion(argumentData: ArgumentData) {}
}
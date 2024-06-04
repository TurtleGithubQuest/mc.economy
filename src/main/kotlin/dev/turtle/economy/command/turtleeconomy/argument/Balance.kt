package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Balance(turtleCommand: TurtleCommand): TurtleSubCommand("balance", turtleCommand) {
    init {
        ArgumentData("player", null, Player::class, isRequired = false)
        ArgumentData("currency", currencies.keys.toList(), String::class, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val target = getValue("player", defaultValue=cs!!.name)?.let { it as CommandSender }?: return true
        val currency = getValue("currency", defaultValue=currencies[currencies.keys.first()])?.toString() ?: return true
        val player = database.getPlayer(target.name)
        val balance = player.getBalance(currency)
        turtle.messageFactory.newMessage("command.turtleeconomy.balance.targets-balance").placeholders(
            hashMapOf("TARGET" to target.name, "BALANCE" to balance)
        ).fromConfig().send(cs!!)
        return true
    }

    override fun onSuggestion(argumentData: ArgumentData) {}
}
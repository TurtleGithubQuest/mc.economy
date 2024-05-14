package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.economy.command.turtleeconomy.TurtleCommandArgument
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Give: TurtleCommandArgument {
    override val usage = "<player> <currency> <item_name> [amount]"

    override fun execute(cs: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        val target = args.getOrNull(1)?.let { Bukkit.getPlayer(it) }?: cs
        val currencyName = args.getOrNull(2)?:
            turtle.messageFactory.newMessage("command.turtleeconomy.give.invalid-currency").enablePrefix().fromConfig().send(cs)

        val itemName = args.getOrNull(3)?:
            turtle.messageFactory.newMessage("command.turtleeconomy.give.invalid-itemname").enablePrefix().fromConfig().send(cs)
        .toString()
        val amount = args.getOrNull(4)?.let { it.toIntOrNull()?: 1 }?: 1
        currencies[currencyName]?.getCurrencyItem(itemName)?.let {
            if (target !is Player)
                turtle.messageFactory.newMessage("command.turtleeconomy.give.noinventory").enablePrefix().fromConfig().send(cs)
            else {
                it.amount(amount)
                target.inventory.addItem(it.getItemStack())
            }
        }?: turtle.messageFactory.newMessage("command.turtleeconomy.give.itemnotfound").enablePrefix().fromConfig().send(cs)
        return true
    }
}
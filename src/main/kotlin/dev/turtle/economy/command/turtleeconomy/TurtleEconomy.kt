package dev.turtle.economy.command.turtleeconomy

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.economy.command.turtleeconomy.argument.Balance
import dev.turtle.economy.command.turtleeconomy.argument.Give
import dev.turtle.economy.command.turtleeconomy.argument.Help
import dev.turtle.turtlelib.TurtleCommand

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class TurtleEconomy: TurtleCommand("turtleeconomy", turtle) {
    init {
        Give(this)
        Balance(this)
        Help(this)
    }
    override fun onCommand(cs: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        return args.getOrNull(0)?.let {
            subCommands[it]?.execute(cs, cmd, label, args)?: run {
                turtle.messageFactory.newMessage("command.turtleeconomy.argument-not-found").enablePrefix().fromConfig().placeholder("ARGUMENT", it).send(cs)
                true
            }
        }?: run {
            turtle.messageFactory.newMessage("command.turtleeconomy.usage").enablePrefix().fromConfig().send(cs)
            true
        }
    }
}
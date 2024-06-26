package dev.turtle.economy.command.turtleeconomy

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.economy.command.turtleeconomy.argument.*
import dev.turtle.turtlelib.command.TurtleCommand

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class TurtleEconomy: TurtleCommand("turtleeconomy", turtle) {
    init {
        Give(this)
        Balance(this)
        Balances(this)
        Wealth(this)
        Transfer(this)
        Reload(this)
        Logs(this)
        Gui(this)
        // Help must be last, all subcommands have to load first.
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
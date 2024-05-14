package dev.turtle.economy.command.turtleeconomy

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.economy.command.turtleeconomy.argument.Give
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class TurtleEconomy: CommandExecutor {
    private val arguments: HashMap<String, TurtleCommandArgument> = hashMapOf("give" to Give())
    override fun onCommand(cs: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        return args.getOrNull(0)?.let {
            arguments[it]?.execute(cs, cmd, label, args)?: run {
                turtle.messageFactory.newMessage("command.turtleeconomy.argument-not-found").enablePrefix().fromConfig().placeholder("ARGUMENT", it).send(cs)
                true
            }
        }?: run {
            turtle.messageFactory.newMessage("command.turtleeconomy.usage").enablePrefix().fromConfig().send(cs)
            true
        }
    }
}
interface TurtleCommandArgument {
    val usage: String
    fun execute(cs: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean
}
package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Balance(turtleCommand: TurtleCommand): TurtleSubCommand("balance", turtleCommand) {
    init {
        ArgumentData("player", null, Player::class, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val target = getValue("player", defaultValue=cs!!.name)?: return true
        println(target)
        turtle.messageFactory.newMessage("bal: 0").send(target as CommandSender)
        return true
    }

    override fun onSuggestion(argumentData: ArgumentData) {
    }
}
package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand

class Reload(turtleCommand: TurtleCommand): TurtleSubCommand("reload", turtleCommand) {
    override fun onCommand(): Boolean {
        turtle.onStart()
        turtle.messageFactory.newMessage("command.turtleeconomy.reload.done").fromConfig().enablePrefix().send(cs!!)
        return true
    }
    override fun onSuggestion(argumentData: ArgumentData) {}
}
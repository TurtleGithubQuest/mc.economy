package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.turtlelib.command.TurtleCommand
import dev.turtle.turtlelib.command.TurtleSubCommand

class Gui(turtleCommand: TurtleCommand): TurtleSubCommand("gui", turtleCommand) {
    init {
        ArgumentData("gui", listOf(), String::class, isRequired = false)
    }

    override fun onCommand(): Boolean { //todo
        val guiName = getValue("gui")?: return true
        val gui = turtle.guis[guiName]?: return true
        gui.openFor(cs!!.name)
        return true
    }
    override fun onSuggestion(argumentData: ArgumentData) {}
}
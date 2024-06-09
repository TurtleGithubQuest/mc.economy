package dev.turtle.economy.gui

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.gui.TurtleGUI
import dev.turtle.economy.gui.slot.action.*
import dev.turtle.economy.gui.slot.behavior.*

class CommandGUI(language: String): TurtleGUI("command", turtle) {
    init {
        DefaultBehavior().register(this)
        CommandAction().register(this)
        this.loadFromConfig(
            turtle.configFactory.get("lang")?.getSection("$language.gui.$name")!!,
            turtle.configFactory.get("config")?.getSection("gui.$name")!!,
        )
    }
}
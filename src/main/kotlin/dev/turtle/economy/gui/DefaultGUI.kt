package dev.turtle.economy.gui

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.economy.gui.slot.behavior.BalancesBehavior
import dev.turtle.turtlelib.gui.GUIBehavior
import dev.turtle.turtlelib.gui.open.slot.action.*
import dev.turtle.turtlelib.gui.open.slot.behavior.*

class DefaultGUI: GUIBehavior("default", turtle) {
    init {
        DefaultBehavior().register(this)
        BalancesBehavior().register(this)
        CommandAction().register(this)
        OpenWindowAction().register(this)
    }
}
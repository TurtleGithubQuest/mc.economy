package dev.turtle.economy.gui

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.gui.GUIBehavior
import dev.turtle.turtlelib.template.gui.action.*
import dev.turtle.turtlelib.template.gui.behavior.*

class DefaultGUI: GUIBehavior("default", turtle) {
    init {
        DefaultBehavior().register(this)
        CommandAction().register(this)
        OpenWindowAction().register(this)
    }
    override fun onGUIRegister() = true
}
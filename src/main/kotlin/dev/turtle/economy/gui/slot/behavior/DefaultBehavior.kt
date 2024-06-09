package dev.turtle.economy.gui.slot.behavior

import com.typesafe.config.Config
import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.event.gui.GUIClickEvent
import dev.turtle.turtlelib.gui.SlotBehavior
import dev.turtle.turtlelib.gui.TurtleGUI
import dev.turtle.turtlelib.gui.TurtleGUI.InventorySlot
import dev.turtle.turtlelib.util.configuration.ConfigUtils.getBooleanOrNull

class DefaultBehavior: SlotBehavior("default") {
    var movement: Boolean = false
        private set

    override var handleClick: (InventorySlot, GUIClickEvent) -> Boolean = { slot, e ->
        e.setCancelled(!movement)
        true
    }
    override var load: (TurtleGUI, InventorySlot, Config) -> SlotBehavior = { gui, slot, cfg ->
        val instance = DefaultBehavior()
        instance.movement = cfg.getBooleanOrNull("movement")?.getValue(turtle.messageFactory)?:false
        instance
    }
}
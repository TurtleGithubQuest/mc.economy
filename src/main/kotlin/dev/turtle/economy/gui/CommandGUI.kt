package dev.turtle.economy.gui

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.gui.TurtleGUI
import org.bukkit.event.inventory.InventoryClickEvent

private val name = "command"
class CommandGUI(language: String): TurtleGUI(name, turtle) {
    init {
        CommandSlotAction()
        DefaultSlotBehavior()
        this.loadFromConfig(
            turtle.configFactory.get("lang")?.getSection("$language.gui.$name")!!,
            turtle.configFactory.get("config")?.getSection("gui.$name")!!,
        )
    }
    inner class DefaultSlotBehavior: TurtleGUI.SlotBehavior(name="default") {
        override var handleClick: (TurtleGUI.InventorySlot, InventoryClickEvent) -> Boolean = { slot, e ->
            println("SlotBehavior: click")
            true
        }
    }
    inner class CommandSlotAction: TurtleGUI.SlotAction(name="command") {
        override var onRun: (TurtleGUI.InventorySlot) -> Boolean = { slot ->
            println("SlotAction ran")
            true
        }
    }
}
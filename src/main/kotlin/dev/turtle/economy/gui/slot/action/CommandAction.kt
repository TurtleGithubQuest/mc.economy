package dev.turtle.economy.gui.slot.action

import com.typesafe.config.Config
import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.event.gui.GUIClickEvent
import dev.turtle.turtlelib.gui.SlotAction
import dev.turtle.turtlelib.gui.TurtleGUI
import dev.turtle.turtlelib.gui.TurtleGUI.InventorySlot
import dev.turtle.turtlelib.util.configuration.ConfigUtils.getStringOrNull
import org.bukkit.Bukkit

class CommandAction: SlotAction(name="command") {
    var command: String = ""
        private set
    var executor: String = ""
        private set

    override var onRun: (InventorySlot, GUIClickEvent) -> Boolean = { slot, e ->
        val cs = if (executor=="SERVER") Bukkit.getConsoleSender() else e.player
        val command = turtle.messageFactory.newMessage(command).placeholder("player", e.player.name).text()
        Bukkit.getScheduler().runTask(turtle, Runnable {
            Bukkit.getServer().dispatchCommand(cs, command)
        })
        true
    }
    override var load: (TurtleGUI, InventorySlot, Config) -> SlotAction = { gui, slot, cfg ->
        val instance = CommandAction()
        instance.command = cfg.getString("command")
        instance.executor = cfg.getStringOrNull("executor").getValue(turtle.messageFactory)?:"PLAYER"
        if (command.startsWith("/"))
            command.drop(1)
        instance
    }
}
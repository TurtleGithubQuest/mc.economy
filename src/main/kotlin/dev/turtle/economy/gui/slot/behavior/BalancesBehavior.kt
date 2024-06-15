package dev.turtle.economy.gui.slot.behavior

import com.typesafe.config.Config
import dev.turtle.turtlelib.gui.InstancedGUI
import dev.turtle.turtlelib.gui.SlotBehavior
import dev.turtle.turtlelib.gui.open.slot.behavior.DefaultBehavior
import dev.turtle.turtlelib.util.configuration.ConfigUtils.getIntOrNull

class BalancesBehavior(name: String="balances"): DefaultBehavior(name) {
    var balanceRank: Int? = null
    override var onLoad: (InstancedGUI, InstancedGUI.InventorySlot, Config, SlotBehavior) -> SlotBehavior = { gui, slot, cfg, instance ->
        val m = gui.behavior.turtle.messageFactory
        if (instance is BalancesBehavior) {
            instance.balanceRank = cfg.getIntOrNull("balance_rank").getValue()
        }
        instance
    }
    override fun newInstance() = BalancesBehavior()
}
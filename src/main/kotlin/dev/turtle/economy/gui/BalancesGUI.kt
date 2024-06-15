package dev.turtle.economy.gui;

import com.typesafe.config.Config
import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.economy.database.BalancesColumn
import dev.turtle.economy.database.OrderBy
import dev.turtle.economy.database.PlayerBalance
import dev.turtle.economy.gui.slot.behavior.BalancesBehavior as SlotBalancesBehavior
import dev.turtle.turtlelib.gui.GUIBehavior
import dev.turtle.turtlelib.gui.InstancedGUI
import dev.turtle.turtlelib.gui.open.slot.action.*
import dev.turtle.turtlelib.gui.open.slot.behavior.*
import dev.turtle.turtlelib.util.extension.TryCatch.tryOrNull
import dev.turtle.turtlelib.util.configuration.ConfigUtils.getIntOrNull
import dev.turtle.turtlelib.util.configuration.ConfigUtils.getStringOrNull
import dev.turtle.turtlelib.util.extension.EnumExtension.valueOfOrNull
import dev.turtle.turtlelib.util.wrapper.CIMutableMap
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

class BalancesGUI: GUIBehavior("balances", turtle) {
    init {
        DefaultBehavior().register(this)
        SlotBalancesBehavior().register(this)
        CommandAction().register(this)
        OpenWindowAction().register(this)
    }
    lateinit var currencyName: String
    var orderColumn: BalancesColumn = BalancesColumn.Balance
    var orderBy: OrderBy = OrderBy.DESC
    var limit = 10
    override fun onRegister(instance: InstancedGUI, cfg: Config): Boolean {
        return (instance.behavior as? BalancesGUI)?.let { behavior ->
            behavior.currencyName = cfg.getStringOrNull("currency_name").getValue()?:currencies.keys.firstOrNull()?:return false
            cfg.getStringOrNull("order_column").getValue()?.let { value ->
                valueOfOrNull<BalancesColumn>(value, true)?.let { behavior.orderColumn = it }
            }
            cfg.getStringOrNull("order_by").getValue()?.let { value ->
                valueOfOrNull<OrderBy>(value, true)?.let { behavior.orderBy = it }
            }
            cfg.getIntOrNull("limit").getValue()?.let { behavior.limit = it }
            true
        }?: false
    }
    override fun beforeOpen(instance: InstancedGUI, player: Player): Boolean {
        val balances = database.getBalances(currencyName, orderColumn, orderBy, limit).toMutableList()
        val currency = currencies[currencyName]?:return false
        val currencyPlaceholders = currency.placeholderMap
        val queue = mutableListOf<InstancedGUI.InventorySlot>()
        fun updateItem(slot: InstancedGUI.InventorySlot, bal: PlayerBalance) {
            slot.behavior.placeholders = CIMutableMap<Any>("player" to bal.nickname, "balance" to bal.balance).apply { this.putAll(currencyPlaceholders) }
            slot.item.loadPlaceholders(turtle.messageFactory, slot.behavior.placeholders).apply {
                //displayName = bal.nickname
                //lore = listOf("Balance: ", bal.balance)
                //material = Material.PLAYER_HEAD
                owningPlayer = bal.uuid.tryOrNull{ UUID.fromString(it.toString())}
            }
        }
        for ((_, slot) in instance.content) {
            val behavior = slot.behavior
            if (behavior !is SlotBalancesBehavior) continue
            val balance = behavior.balanceRank?.let { try { balances.removeAt(it-1/*this is probably more user-friendly*/) } catch (_: IndexOutOfBoundsException) {null} }
            if (balance == null) {
                queue.add(slot)
                continue
            }
            updateItem(slot, balance)
        }
        queue.forEach { slot ->
            balances.removeFirstOrNull()?.let { updateItem(slot, it) }?:slot.item.apply{material=Material.AIR}
        }
        return true
    }
}

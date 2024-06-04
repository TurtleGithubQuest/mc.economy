package dev.turtle.economy.currency

import dev.turtle.economy.Economy.Companion.cfg
import dev.turtle.economy.Economy.Companion.database
import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.economy.database.BalanceChange
import dev.turtle.economy.database.Via
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.math.BigDecimal
import kotlin.math.pow
import kotlin.math.roundToInt

class Currency(val name: String) {
    val startBalance: Int
    val symbol: String
    val decimals: Int
    val zeroPow: Double
    val minimalValue: String
    val blacklist: Array<String>
    val items: HashMap<String, CurrencyItem> = hashMapOf()
    val placeholderMap = mutableMapOf<String, String>()
    companion object {
        val nskCurrencyItem = NamespacedKey(turtle, "CurrencyItem")
        val nskCurrencyItemCreatedBy = NamespacedKey(turtle, "CurrencyItemCreatedBy")
    }
    init {
        cfg.getSection("currency.$name")!!.let { currency ->
            symbol = currency.getString("symbol")
            decimals = currency.getInt("decimals")
            zeroPow = 10.0.pow(decimals)
            minimalValue = BigDecimal.valueOf(1, decimals).toString()
            placeholderMap["SYMBOL"] = symbol
            startBalance = currency.getInt("balance.start")
            blacklist = currency.getList("blacklist").unwrapped().map { it.toString() }.toTypedArray()
            currency.getConfig("items")?.let {
                itemsConfig ->
                 itemsConfig.root().entries.forEach { (itemName, _) ->
                     val item = itemsConfig.getConfig(itemName)!!
                     items[itemName.uppercase()] = CurrencyItem(itemName)
                         .displayName(item.getString("displayName"))
                         .material(item.getString("material") ?: "STONE")
                 }
            }
        }
        turtle.messageFactory.newMessage(
            "&7Loaded &e${items.size}&7 items for currency &e$name&7."
        ).enablePrefix().send()
    }
    fun getCurrencyItem(itemName: String): CurrencyItem? { return items[itemName.uppercase()] }
    fun getCurrencyItem(itemStack: ItemStack?): CurrencyItem? {
        return itemStack?.itemMeta?.persistentDataContainer?.get(nskCurrencyItem, PersistentDataType.STRING)?.let {
            items[it.uppercase()]
        }
    }
    fun getAmountBigInt(amount: Double): Int = this.zeroPow.times(amount).roundToInt()
    fun getAmountBigInt(amount: String): Int? = amount.toDoubleOrNull()?.let {getAmountBigInt(it)}
    fun getAmountForHuman(amount: Any): String = amount.toString().toDoubleOrNull()?.div(this.zeroPow).toString()
    fun hasExcessiveDecimals(amount: Double): Boolean = BigDecimal.valueOf(amount).scale() > this.decimals

    inner class CurrencyItem(private val itemName: String) {
        private var displayName: String? = null
        private var material: String = "STONE"
        private var amount: Int = 1
        private var value = 1.0
        fun amount(value: Int): CurrencyItem {
            this.amount = value; return this
        }
        fun value(value: Double): CurrencyItem {
            this.value = value; return this
        }
        fun material(value: String): CurrencyItem {
            this.material = value; return this
        }
        fun displayName(newDisplayName: String?): CurrencyItem {
            this.displayName = newDisplayName?.let {
                turtle.messageFactory.newMessage(it)
                    .placeholder("value", value.toString())
                    .placeholders(HashMap(placeholderMap))
                    .text()
            }
            return this
        }
        fun getItemStack(createdBy: String = "SERVER"): ItemStack {
            val itemStack = ItemStack(Material.matchMaterial(material)?: Material.STONE)
            itemStack.itemMeta?.let { itemMeta ->
                itemMeta.setDisplayName(displayName)
                val data = itemMeta.persistentDataContainer
                data.set(nskCurrencyItem, PersistentDataType.STRING, itemName)
                data.set(nskCurrencyItemCreatedBy, PersistentDataType.STRING, createdBy)
                itemStack.itemMeta = itemMeta
            }
            itemStack.amount = this.amount
            return itemStack
        }
        fun onInteract(e: PlayerInteractEvent) {
            when (e.action) {
                Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
                    if (database.getPlayer(e.player.name).updateBalance(BalanceChange.INC, name, getAmountBigInt(value),
                            (e.item?.itemMeta?.persistentDataContainer?.get(nskCurrencyItemCreatedBy, PersistentDataType.STRING))?:"SERVER", Via.ITEM)) {
                        turtle.messageFactory.newMessage("action.currency-item-claim.success")
                            .placeholder("value", value.toString())
                            .placeholder("item_name", displayName ?: material)
                            .placeholders(HashMap(placeholderMap))
                            .fromConfig()
                            .send(e.player)
                        e.item?.let { it.amount -= 1 }
                    } else
                        turtle.messageFactory.newMessage("action.currency-item-claim.error")
                            .placeholder("value", value.toString())
                            .placeholder("item_name", displayName ?: material)
                            .placeholders(HashMap(placeholderMap))
                            .fromConfig()
                            .send(e.player)
                }
                else -> {}
            }
        }
    }
    inner class CurrencyItemListener(): Listener {
        @EventHandler
        fun onInteraction(e: PlayerInteractEvent) {
            getCurrencyItem(e.item)?.onInteract(e)
        }
    }
}
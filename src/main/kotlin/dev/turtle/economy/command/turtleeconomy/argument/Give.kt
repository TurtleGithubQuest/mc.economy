package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.turtlelib.command.TurtleCommand
import dev.turtle.turtlelib.command.TurtleSubCommand
import org.bukkit.entity.Player

class Give(turtleCommand: TurtleCommand): TurtleSubCommand("give", turtleCommand) {
    init {
        ArgumentData("player", null, Player::class)
        ArgumentData("currency", currencies.keys.toList(), String::class)
        ArgumentData("item_name", listOf() /*todo*/, String::class)
        ArgumentData("amount", listOf(1, 16, 32, 64), Int::class, defaultValue = 1, isRequired = false)
    }
    override fun onCommand(): Boolean {
        val target = getValue("player", cs!!.name)?: return true
        val currencyName = getValue("currency")?.toString() ?: return true
        val itemName = getValue("item_name")?.toString() ?: return true
        val amount = getValue("amount")?.let { it as Int } ?: return true
        val placeholders: HashMap<String, Any> = hashMapOf(
            "TARGET" to target,
            "ITEM_NAME" to itemName,
            "CURRENCY" to currencyName,
            "AMOUNT" to amount,
            "ITEMS" to currencies[currencyName]?.items?.keys?.joinToString(", ") as Any
        )
        currencies[currencyName]?.getCurrencyItem(itemName)
            ?.let {
                if (target !is Player)
                    turtle.messageFactory.newMessage("command.turtleeconomy.give.invalid-target")
                        .placeholders(placeholders)
                        .fromConfig().send(cs!!)
                else {
                    turtle.messageFactory.newMessage("command.turtleeconomy.give.success")
                        .placeholders(placeholders).placeholder("target", target.name)
                        .fromConfig().send(cs!!)
                    it.amount(amount)
                    target.inventory.addItem(it.getItemStack(cs!!.name))
                }
            } ?: turtle.messageFactory.newMessage("command.turtleeconomy.give.item-not-found")
                    .placeholders(placeholders)
                    .fromConfig().send(cs!!)
        return true
    }
    override fun onSuggestion(argumentData: ArgumentData) {
        when (argumentData.argumentName) {
            "currency" -> {
                argumentData.getValue(notifyErrors=false)?.let { currencyName ->
                    super.argumentData["item_name"]?.updateSuggestions(currencies[currencyName.toString().uppercase()]?.items?.keys?.toList())
                }
            }
            else -> {}
        }
    }
}
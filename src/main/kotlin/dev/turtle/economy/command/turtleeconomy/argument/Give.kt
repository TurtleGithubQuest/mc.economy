package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand
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
            "ITEM_NAME" to itemName,
            "CURRENCY" to currencyName,
            "AMOUNT" to amount
        )
        currencies[currencyName]?.getCurrencyItem(itemName)
            ?.let {
                if (target !is Player)
                    turtle.messageFactory.newMessage("command.turtleeconomy.give.invalid-target").fromConfig().send(cs!!)
                else {
                    turtle.messageFactory.newMessage("command.turtleeconomy.give.success")
                        .placeholders(placeholders).placeholder("target", target.name).fromConfig()
                        .send(cs!!)
                    it.amount(amount)
                    target.inventory.addItem(it.getItemStack())
                }
            } ?: turtle.messageFactory.newMessage("command.turtleeconomy.give.item-not-found")
                    .placeholder("item_name", itemName)
                    .fromConfig().send(cs!!)
        return true
    }
    override fun onSuggestion(argumentData: ArgumentData) {
        when (argumentData.argumentName) {
            "currency" -> {
                argumentData.getValue(notifyErrors=false)?.let { currencyName ->
                    super.argumentData["item_name"]?.updateSuggestions(currencies[currencyName]?.items?.keys?.toList())
                }
            }
            else -> {}
        }
    }
}
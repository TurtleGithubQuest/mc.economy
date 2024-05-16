package dev.turtle.economy

import dev.turtle.economy.command.turtleeconomy.TurtleEconomy
import dev.turtle.economy.currency.Currency
import dev.turtle.economy.database.Database
import dev.turtle.turtlelib.TurtlePlugin
import dev.turtle.turtlelib.util.configuration.Configuration
import org.bukkit.Bukkit
import org.bukkit.event.Listener

class Economy: TurtlePlugin() {
    companion object {
        lateinit var cfg: Configuration
        lateinit var lang: Configuration
        lateinit var database: Database
        val currencies = mutableMapOf<String, Currency>()
        val eventListeners = mutableListOf<Listener>()
        lateinit var turtle: TurtlePlugin
    }
    override fun onStart() {
        turtle = this
        lang = ConfigLang()
        cfg = ConfigConfig()
        configFactory.reload()
        eventListeners.forEach { Bukkit.getPluginManager().registerEvents(it, this)}
        getCommand("turtleeconomy")!!.setExecutor(TurtleEconomy())
        messageFactory
            .setPrefix("&8&l[&2Turtle&9Economy&8&l]&7 ")
            .newMessage("&7v&b$pluginVersion &2enabled&7.").enablePrefix().send()
    }
    inner class ConfigConfig: Configuration("config", configFactory) {
        override fun onConfigurationLoad() {
            this.getSection("currency")?.let {
                for ((currencyName, _) in it.root().entries) {
                    val currency = Currency(currencyName)
                    eventListeners.add(currency.CurrencyItemListener())
                    currencies[currencyName.uppercase()] = currency
                }
                plugin.messageFactory.newMessage(
                    "&7Loaded &e${currencies.size}&7 currencies."
                ).enablePrefix().send()
            }?: this@Economy.disable("&7Plugin &cdisabled&7: No currency configuration found.")
            this.getSection("database")?.let {

            }?: this@Economy.disable("&7Plugin &cdisabled&7: No database configuration found.")
        }
    }
    inner class ConfigLang: Configuration("lang", configFactory) {
        override fun onConfigurationLoad() {}
    }
}

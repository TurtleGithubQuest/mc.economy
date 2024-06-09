package dev.turtle.economy

import com.typesafe.config.ConfigException
import dev.turtle.economy.command.turtleeconomy.TurtleEconomy
import dev.turtle.economy.currency.Currency
import dev.turtle.economy.database.TEcoDatabase
import dev.turtle.economy.event.player.join.PlayerJoin
import dev.turtle.economy.gui.CommandGUI
import dev.turtle.turtlelib.TurtlePlugin
import dev.turtle.turtlelib.util.configuration.Configuration

class Economy: TurtlePlugin() {
    companion object {
        lateinit var cfg: Configuration
        lateinit var lang: Configuration
        lateinit var database: TEcoDatabase
        val currencies = mutableMapOf<String, Currency>()
        lateinit var turtle: TurtlePlugin
    }
    override fun onStart() {
        turtle = this
        messageFactory
            .setPrefix("&8&l[&2Turtle&9Economy&8&l]&7 ")
            .enableAlignment()
        currencies.clear()
        lang = ConfigLang()
        cfg = ConfigConfig()
        configFactory.reload()
        getCommand("turtleeconomy")!!.setExecutor(TurtleEconomy())
        messageFactory
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
                messageFactory.newMessage(
                    "&7Loaded &e${currencies.size}&7 currencies."
                ).enablePrefix().send()
            }?: this@Economy.disable("&7Plugin &cdisabled&7: No currency configuration found.")
            this.getSection("gui")?.let {
                try {
                    val guiArray = arrayOf(CommandGUI("en_US")) //todo
                    messageFactory.newMessage(
                        "&7Loaded &e${guiArray.size}&7 GUI${if (guiArray.size > 1) "s" else ""}: &7${guiArray.joinToString("&8, "){ it.name } }&7."
                    ).enablePrefix().send()
                } catch (ex: NullPointerException) {
                    ex.printStackTrace()
                    this@Economy.disable("&7Plugin &cdisabled&7: Failed to load GUI configuration.")
                }
            }?: this@Economy.disable("&7Plugin &cdisabled&7: No GUI configuration found.")
            this.getSection("database")?.let { dbCfg ->
                try {
                    when (dbCfg.getString("type").lowercase()) {
                        "mysql" ->
                            database = TEcoDatabase(
                                dbCfg.getString("name"),
                                dbCfg.getString("ip"),
                                this.getStringOrNull("database.port"),
                                dbCfg.getString("ssl"),
                            )
                        else -> this@Economy.disable("&7Plugin &cdisabled&7: Invalid database type '&e${dbCfg.getString("dbType")}&7'.")
                    }
                    database.connect(dbCfg.getString("username"), dbCfg.getString("password"))
                    database.migrate()
                    eventListeners.add(PlayerJoin())
                } catch(ex: ConfigException) {
                    this@Economy.disable("&7Plugin &cdisabled&7: ${ex.message}.")
                }
            }?: this@Economy.disable("&7Plugin &cdisabled&7: No database configuration found.")
        }
    }
    inner class ConfigLang: Configuration("lang", configFactory) {
        override fun onConfigurationLoad() {}
    }
}

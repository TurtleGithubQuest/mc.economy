package dev.turtle.economy.event.player.join

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.database
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoin: Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val p = e.player
        currencies.forEach { (currencyName, currency) ->
            database.initBalance(p.name, currencyName, currency.startBalance)
        }
    }
}
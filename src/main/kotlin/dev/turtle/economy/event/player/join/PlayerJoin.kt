package dev.turtle.economy.event.player.join

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoin: Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        /*val p = e.player
        currencies.forEach { (currencyName, currency) ->
            database.getPlayer(p.name, p.uniqueId.toString()).initBalance(
                currencyName, currency.startBalance
            )
        }*/
    }
}
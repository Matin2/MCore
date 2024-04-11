package me.matin.core.managers.menu

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class MenuManager: Listener {

    @EventHandler
    private fun onInventoryDrag(e: InventoryDragEvent) {
        val topInv = e.whoClicked.openInventory.topInventory
        if (topInv.holder !is Menu) return
        for (slot in e.rawSlots) {
            if (slot in 0..<topInv.size) e.isCancelled = true
        }
    }

    @EventHandler
    private fun onInventoryClick(e: InventoryClickEvent) {
        if (e.currentItem == null) return
        val topInv = e.whoClicked.openInventory.topInventory
        val holder = topInv.holder
        if (holder !is Menu) return
        holder.cancelClick(e)
        holder.handleMenu(e)
    }

    companion object {

        private var playerMenuUtilityMap: HashMap<Player, PlayerMenuUtility> = HashMap()

        @JvmStatic
        fun getPlayerMenuUtility(player: Player): PlayerMenuUtility {
            if (player in playerMenuUtilityMap) {
                var playerMenuUtility = playerMenuUtilityMap[player]
                if (playerMenuUtility == null) {
                    playerMenuUtility = PlayerMenuUtility(player)
                    playerMenuUtilityMap[player] = playerMenuUtility
                }
                return playerMenuUtility
            } else {
                val playerMenuUtility = PlayerMenuUtility(player)
                playerMenuUtilityMap[player] = playerMenuUtility
                return playerMenuUtility
            }
        }
    }
}
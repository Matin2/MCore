package me.matin.core.menu

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class MenuListeners: Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val p = e.whoClicked as Player
        val inv = e.clickedInventory ?: return
        val holder = inv.holder
        if (holder is Menu) {
            e.isCancelled = true
            if (e.currentItem == null) return
            holder.handleMenu(e)
        }
    }
}
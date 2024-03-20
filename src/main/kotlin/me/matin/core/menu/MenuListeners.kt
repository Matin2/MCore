package me.matin.core.menu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class MenuListeners: Listener {

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        val topInv = e.whoClicked.openInventory.topInventory
        if (topInv.holder !is Menu) return
        for (slot in e.rawSlots) {
            if (slot in 0..<topInv.size) e.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.currentItem == null) return
        val topInv = e.whoClicked.openInventory.topInventory
        val holder = topInv.holder
        if (holder !is Menu) return
        holder.cancelClick(e)
        holder.handleMenu(e)
    }
}
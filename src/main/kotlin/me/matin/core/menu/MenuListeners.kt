package me.matin.core.menu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
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
        val topInv = e.whoClicked.openInventory.topInventory
        val holder = topInv.holder
        if (holder !is Menu) return
        val inv = e.clickedInventory ?: return
        if (inv == e.whoClicked.openInventory.bottomInventory) {
            if (e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                e.isCancelled = true
            }
        }
        if (inv == topInv && e.slot !in holder.noCancelClickSlots) e.isCancelled = true
        if (e.currentItem == null) return
        holder.handleMenu(e)
    }
}
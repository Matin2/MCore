package me.matin.core.managers.menu

import me.matin.core.managers.item.ItemManager
import me.matin.core.managers.menu.MenuType.entries
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType

@Suppress("unused")
object MenuManager: Listener {

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        val topInv = e.whoClicked.openInventory.topInventory.takeIf { it.holder is Menu } ?: return
        e.rawSlots.forEach {
            if (it in 0..<topInv.size) e.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        e.currentItem ?: return
        (e.whoClicked.openInventory.topInventory.holder as? Menu)?.apply {
            cancelClick(e)
            handleMenu(e)
        }
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        checkCursor(player)
    }

    @JvmStatic
    operator fun get(rows: Int): MenuType {
        val r = minOf(maxOf(rows, 1), 6)
        return entries.first { it.rows == r }
    }

    @JvmStatic
    operator fun get(type: InventoryType): MenuType? {
        return entries.firstOrNull { it.type == type }
    }

    fun checkCursor(player: Player) {
        val cursor = player.openInventory.cursor.takeUnless { it.isEmpty || it.type.isAir } ?: return
        val holder = player.openInventory.topInventory.holder as? Menu ?: return
        holder.takeIf { it.antiCursorItemLoss } ?: return
        player.inventory.addItem(cursor).takeUnless { it.isEmpty() }
            ?: ItemManager.drop(cursor, player.location , BlockFace.UP)
    }
}
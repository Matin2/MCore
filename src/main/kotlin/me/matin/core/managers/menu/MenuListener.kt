package me.matin.core.managers.menu

import me.matin.core.managers.item.ItemManager
import me.matin.core.managers.menu.handlers.MenuHandler
import me.matin.core.managers.opt
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

@Suppress("unused")
object MenuListener: Listener {

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        val topInv = e.whoClicked.openInventory.topInventory
        val handler = topInv.holder as? MenuHandler ?: return
        handler.manageBehavior(e)
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val inv = e.clickedInventory ?: return
        val bottomInv = e.whoClicked.openInventory.bottomInventory
        val topInv = e.whoClicked.openInventory.topInventory
        val handler = topInv.holder as? MenuHandler ?: return
        if (inv == bottomInv && handler.menu.freezeBottomInv) e.isCancelled = true
        handler.manageBehavior(e)
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        checkCursor(player)
        val handler = player.openInventory.topInventory.holder as? MenuHandler ?: return
        handler.close(false)
    }

    @JvmStatic
    fun checkCursor(player: Player) {
        val handler = player.openInventory.topInventory.holder as? MenuHandler ?: return
        val cursor = player.openInventory.cursor.takeUnless { it.isEmpty || it.type.isAir } ?: return
        handler.takeIf { it.menu.preventCursorLoss } ?: return
        player.inventory.addItem(cursor).takeUnless { it.isEmpty() } ?: ItemManager.drop(
            cursor, player.location, BlockFace.UP.opt
        )
    }
}
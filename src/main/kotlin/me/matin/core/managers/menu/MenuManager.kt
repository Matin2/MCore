package me.matin.core.managers.menu

import me.matin.core.managers.item.ItemManager
import me.matin.core.managers.menu.menus.ListMenu
import me.matin.core.managers.menu.menus.Menu
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

@Suppress("unused")
object MenuManager: Listener {

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        val menu = e.whoClicked.openInventory.topInventory.takeIf { it.holder is InventoryMenu } ?: return
        when (menu) {
            is Menu -> menu.manageBehavior(e)
            is ListMenu<*> -> menu.manageBehavior(e)
        }
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val inv = e.clickedInventory ?: return
        val bottomInv = e.whoClicked.openInventory.bottomInventory
        val topInv = e.whoClicked.openInventory.topInventory
        val menu = topInv.holder as? InventoryMenu ?: return
        if (inv == bottomInv && menu.freezeBottomInv) e.isCancelled = true
        when (menu) {
            is Menu -> menu.manageBehavior(e)
            is ListMenu<*> -> menu.manageBehavior(e)
        }
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        checkCursor(player)
        val menu = player.openInventory.topInventory.holder as? InventoryMenu ?: return
        when (menu) {
            is Menu -> menu.close(false)
            is ListMenu<*> -> menu.close(false)
        }
    }

    @JvmStatic
    fun checkCursor(player: Player) {
        val cursor = player.openInventory.cursor.takeUnless { it.isEmpty || it.type.isAir } ?: return
        val holder = player.openInventory.topInventory.holder as? Menu ?: return
        holder.takeIf { it.preventCursorLoss } ?: return
        player.inventory.addItem(cursor).takeUnless { it.isEmpty() } ?: ItemManager.drop(
            cursor, player.location, BlockFace.UP
        )
    }
}
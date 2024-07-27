package me.matin.core.managers.menu

import me.matin.core.managers.item.ItemManager
import me.matin.core.managers.menu.items.button.ButtonManager
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*

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
        val inv = e.clickedInventory ?: return
        val bottomInv = e.whoClicked.openInventory.bottomInventory
        val topInv = e.whoClicked.openInventory.topInventory
        val menu = topInv.holder as? InventoryMenu ?: return
        if (inv == bottomInv && (menu.freezeBottomInv || e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY))
            e.isCancelled = true
        if (inv != topInv) return
        ButtonManager(menu).manageBehavior(e)
        if (menu is ListMenu<*>) menu.manageBehaviour(e)
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
    fun getType(rows: Int): MenuType = MenuType.entries.first { it.rows == minOf(maxOf(rows, 1), 6) }

    @JvmStatic
    fun getType(type: InventoryType): MenuType = MenuType.entries.firstOrNull { it.type == type } ?: MenuType.THREE

    fun checkCursor(player: Player) {
        val cursor = player.openInventory.cursor.takeUnless { it.isEmpty || it.type.isAir } ?: return
        val holder = player.openInventory.topInventory.holder as? Menu ?: return
        holder.takeIf { it.preventCursorLoss } ?: return
        player.inventory.addItem(cursor).takeUnless { it.isEmpty() }
            ?: ItemManager.drop(cursor, player.location, BlockFace.UP)
    }
}
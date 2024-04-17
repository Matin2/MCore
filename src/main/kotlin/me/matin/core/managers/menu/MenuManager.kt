package me.matin.core.managers.menu

import me.matin.core.managers.item.ItemManager
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
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
        val menu = topInv.holder as? Menu ?: return
        menu.cancelClick(e)
        menu.handleMenu(e)
    }

    @EventHandler
    private fun onInventoryClose(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        checkCursor(player)
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

        @JvmStatic
        fun checkCursor(player: Player) {
            val cursor = player.openInventory.cursor
            val inv = player.openInventory.topInventory
            val menu = inv.holder as? Menu ?: return
            if (!menu.antiCursorItemLoss) return
            if (!cursor.isEmpty && !cursor.type.isAir) {
                val result = player.inventory.addItem(cursor)
                if (result.isNotEmpty()) ItemManager.drop(cursor, player.location , BlockFace.UP)
            }
        }
    }
}
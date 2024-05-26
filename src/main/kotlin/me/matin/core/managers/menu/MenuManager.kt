package me.matin.core.managers.menu

import me.matin.core.managers.item.ItemManager
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

@Suppress("unused")
class MenuManager: Listener {

    @EventHandler
    private fun onInventoryDrag(e: InventoryDragEvent) {
        val topInv = e.whoClicked.openInventory.topInventory.takeIf { it.holder is Menu } ?: return
        e.rawSlots.forEach {
            if (it in 0..<topInv.size) e.isCancelled = true
        }
    }

    @EventHandler
    private fun onInventoryClick(e: InventoryClickEvent) {
        e.currentItem ?: return
        (e.whoClicked.openInventory.topInventory.holder as? Menu)?.apply {
            cancelClick(e)
            handleMenu(e)
        }
    }

    @EventHandler
    private fun onInventoryClose(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        checkCursor(player)
    }

    companion object {

        private var playerMenuUtilMap: HashMap<Player, PlayerMenuUtil> = HashMap()

        @JvmStatic
        fun getPlayerMenuUtil(player: Player): PlayerMenuUtil {
            if (player !in playerMenuUtilMap) {
                val playerMenuUtil = PlayerMenuUtil(player)
                playerMenuUtilMap[player] = playerMenuUtil
                return playerMenuUtil
            }
            return playerMenuUtilMap[player]!!
        }

        @JvmStatic
        fun checkCursor(player: Player) {
            val cursor = player.openInventory.cursor.takeUnless { it.isEmpty || it.type.isAir } ?: return
            val holder = player.openInventory.topInventory.holder as? Menu ?: return
            holder.takeIf { it.antiCursorItemLoss } ?: return
            player.inventory.addItem(cursor).takeUnless { it.isEmpty() }
                ?: ItemManager.drop(cursor, player.location , BlockFace.UP)
        }
    }
}
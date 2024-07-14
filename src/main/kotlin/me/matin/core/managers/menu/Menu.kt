package me.matin.core.managers.menu

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

@Suppress("unused")
abstract class Menu(private var playerMenuUtil: PlayerMenuUtil): InventoryHolder {

    private lateinit var inventory: Inventory
    abstract val title: Component
    abstract val type: MenuType
    open val cancelClickIgnoredSlots: ArrayList<Int> = ArrayList()
    open val freezeBottomInv: Boolean = false
    open val antiCursorItemLoss: Boolean = true

    abstract fun handleMenu(event: InventoryClickEvent)
    abstract fun setMenuItems()

    fun open() {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: {
            inventory = Bukkit.createInventory(this, type.rows!! * 9, title)
        }
        setMenuItems()
        playerMenuUtil.player.openInventory(inventory)
    }

    open fun cancelClick(event: InventoryClickEvent) {
        val topInv = event.whoClicked.openInventory.topInventory
        val inv = event.clickedInventory ?: return
        if (inv == event.whoClicked.openInventory.bottomInventory) {
            if (freezeBottomInv || event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
                event.isCancelled = true
        } else if (inv == topInv && event.slot !in cancelClickIgnoredSlots) event.isCancelled = true
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}
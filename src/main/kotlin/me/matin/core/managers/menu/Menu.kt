package me.matin.core.managers.menu

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class Menu(private var playerMenuUtility: PlayerMenuUtility): InventoryHolder {

    private lateinit var inventory: Inventory

    abstract val title: Component

    abstract val type: MenuType
    open var rows: Int = 3

    open val cancelClickIgnoredSlots: ArrayList<Int> = ArrayList()
    open val freezeBottomInv: Boolean = false

    abstract fun handleMenu(event: InventoryClickEvent)
    abstract fun setMenuItems()

    fun open() {
        if (type == MenuType.NORMAL) {
            if (rows < 1) rows = 1
            if (rows > 6) rows = 6
            inventory = Bukkit.createInventory(this, rows * 9, title)
        } else {
            inventory = Bukkit.createInventory(this, type.type, title)
        }
        setMenuItems()
        playerMenuUtility.player.openInventory(inventory)
    }

    open fun cancelClick(event: InventoryClickEvent) {
        val topInv = event.whoClicked.openInventory.topInventory
        val inv = event.clickedInventory ?: return
        if (inv == event.whoClicked.openInventory.bottomInventory) {
            if (freezeBottomInv) event.isCancelled = true
            else if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.isCancelled = true
            }
        } else if (inv == topInv && event.slot !in cancelClickIgnoredSlots) event.isCancelled = true
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}
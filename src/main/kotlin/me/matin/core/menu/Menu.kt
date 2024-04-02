package me.matin.core.menu

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class Menu(private var playerMenuUtility: PlayerMenuUtility.MenuUtility): InventoryHolder {

    private lateinit var inventory: Inventory

    abstract val title: Component

    abstract val rowsOrType: Any

    open val cancelClickIgnoredSlots: ArrayList<Int> = ArrayList()
    open val freezeBottomInv: Boolean = false

    abstract fun handleMenu(event: InventoryClickEvent)
    abstract fun setMenuItems()

    fun open() {
        if (rowsOrType !is Int && rowsOrType !is InventoryType) throw IllegalArgumentException("'rowsOrType' must be either Integer or InventoryType")
        if (rowsOrType is Int && rowsOrType !in 1..6) throw IllegalArgumentException("'rows' must be between 1 and 6")
        if (rowsOrType is Int && rowsOrType in 1..6) {
            inventory = Bukkit.createInventory(this, rowsOrType as Int * 9, title)
        } else if (rowsOrType is InventoryType) {
            inventory = Bukkit.createInventory(this, rowsOrType as InventoryType, title)
        }
        this.setMenuItems()
        playerMenuUtility.owner.openInventory(inventory)
    }

    open fun cancelClick(event: InventoryClickEvent) {
        val topInv = event.whoClicked.openInventory.topInventory
        val inv = event.clickedInventory ?: return
        if (inv == event.whoClicked.openInventory.bottomInventory) {
            if (freezeBottomInv) event.isCancelled = true
            else if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.isCancelled = true
            }
        }
        if (inv == topInv && event.slot !in cancelClickIgnoredSlots) event.isCancelled = true
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}
package me.matin.core.menu

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class Menu(protected var playerMenuUtility: PlayerMenuUtility): InventoryHolder {

    protected lateinit var inventory: Inventory

    abstract val name: Component
    abstract val rows: Int
    abstract fun handleMenu(event: InventoryClickEvent)
    abstract fun setMenuItems()
    fun open() {
        inventory = Bukkit.createInventory(this, rows * 9, name)
        this.setMenuItems()
        playerMenuUtility.getOwner().openInventory(inventory)
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}
package me.matin.core.managers.menu

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

@Suppress("unused")
abstract class Menu(private var playerMenuUtil: MenuUtil): InventoryHolder {

    private lateinit var inventory: Inventory
    abstract val title: Component
    abstract val type: MenuType
    abstract val buttons: List<Button>
    open val cancelClickIgnoredSlots: ArrayList<Int> = ArrayList()
    open val freezeBottomInv: Boolean = false
    open val antiCursorItemLoss: Boolean = true

    fun open() {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: run {
            inventory = Bukkit.createInventory(this, type.rows!! * 9, title)
        }
        buttons.forEach {
            it.items.forEach { (item, slot) ->
                inventory.setItem(slot, item)
            }
        }
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
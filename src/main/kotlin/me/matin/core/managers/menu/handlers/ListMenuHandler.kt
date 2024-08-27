package me.matin.core.managers.menu.handlers

import me.matin.core.managers.menu.items.other.MenuList
import me.matin.core.managers.menu.menus.ListMenu
import me.matin.core.methods.schedule
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryInteractEvent

class ListMenuHandler<T>(override val menu: ListMenu<T>): MenuHandler(menu) {

    private val listManager by lazy { MenuList.Manager<T>() }

    override fun open() {
        listManager.makeListMap(menu.list)
        super.open()
    }

    override fun manageBehavior(event: InventoryInteractEvent) {
        super.manageBehavior(event)
        listManager.manageBehavior(event as? InventoryClickEvent ?: return, menu.list, menu.page)
    }

    fun updatePage() {
        schedule(true) {
            listManager.manageDisplay(inventory, menu.list, menu.page)
            val fillerSlots = (0..<inventory.size).toMutableSet()
            fillerSlots.removeAll(menu.list.slots)
            super.updateItems(false, fillerSlots)
        }
    }

    override fun updateItems(useDefaultItem: Boolean, fillerSlots: MutableSet<Int>) {
        menu.updatePages()
        listManager.manageDisplay(inventory, menu.list, menu.page)
        fillerSlots.removeAll(menu.list.slots)
        super.updateItems(useDefaultItem, fillerSlots)
    }
}
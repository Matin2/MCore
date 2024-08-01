package me.matin.core.managers.menu.handlers

import me.matin.core.Core
import me.matin.core.managers.menu.items.other.MenuList
import me.matin.core.managers.menu.menus.ListMenu
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryInteractEvent

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ListMenuHandler<T>(override val menu: ListMenu<T>): MenuHandler(menu) {

    private lateinit var fillerSlots: Set<Int>
    private var listManager = MenuList.Manager<T>()
    var pages: Int = 0
    var page: Int = menu.page
        set(value) {
            pages = (menu.list.list.size / menu.list.slots.size) + 1
            field = when {
                value < 0 -> (value % pages) + pages
                value >= pages -> value % pages
                else -> value
            }
            Core.scheduleTask(true) { privateUpdateItems() }
        }

    override fun open() {
        listManager.makeListMap(menu.list)
        super.open()
    }

    override fun manageBehavior(event: InventoryInteractEvent) {
        super.manageBehavior(event)
        listManager.manageBehavior(event as? InventoryClickEvent ?: return, menu.list, page)
    }

    private fun privateUpdateItems() {
        listManager.manageDisplay(inventory, menu.list, page)
        val fillerSlots = (0..<inventory.size).toMutableSet()
        fillerSlots.removeAll(menu.list.slots)
        super.updateItems(false, fillerSlots)
    }

    override fun updateItems(useDefaultItem: Boolean, fillerSlots: MutableSet<Int>) {
        pages = (menu.list.list.size / menu.list.slots.size) + 1
        listManager.manageDisplay(inventory, menu.list, page)
        fillerSlots.removeAll(menu.list.slots)
        super.updateItems(useDefaultItem, fillerSlots)
    }
}
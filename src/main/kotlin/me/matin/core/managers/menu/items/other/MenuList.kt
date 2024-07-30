package me.matin.core.managers.menu.items.other

import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.menus.ListMenu
import me.matin.core.managers.menu.utils.DisplayItem
import org.bukkit.event.inventory.InventoryClickEvent

typealias ListMap = Map<Int, List<Pair<Int, Int>>>

class MenuList<T>(
    val slots: Set<Int>,
    val list: List<T>,
    val display: (T) -> DisplayItem,
    val interactAction: Interacted.(T) -> Unit = {}
) {

    class Manager<T> {

        fun manageDisplay(menu: ListMenu<T>, map: ListMap) {
            val map1 = menu.list.run {
                slots - map.getValue(menu.page).map {
                    menu.inventory.setItem(it.second, display(list[it.first]).toItem())
                    it.second
                }.toSet()
            }
            map1.forEach { menu.inventory.setItem(it, menu.listFiller.display.toItem()) }
        }

        fun manageBehaviour(event: InventoryClickEvent, menu: ListMenu<T>, map: ListMap) {
            if (event.slot !in menu.list.slots) return
            event.isCancelled = true
            val action = ButtonAction[event.click, event.hotbarButton] ?: return
            if (event.currentItem?.type == menu.listFiller.display.material) {
                menu.listFiller.interactAction(Interacted(event, action))
                return
            }
            val (index) = map.getValue(menu.page).first { it.second == event.slot }
            menu.list.apply {
                interactAction(Interacted(event, action), list[index])
            }
        }
    }
}

package me.matin.core.managers.menu.items.other

import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.utils.DisplayItem
import me.matin.core.managers.menu.utils.Interacted
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

typealias ListMap = Map<Int, List<Pair<Int, Int>>>

class MenuList<T>(
    val slots: Set<Int>,
    val list: List<T>,
    val display: (T) -> DisplayItem,
    val filler: Filler = Filler(),
    val interactAction: Interacted.(T) -> Unit = {}
) {

    class Manager<T> {

        private var listMap: ListMap? = null

        fun makeListMap(list: MenuList<T>) {
            val slots = list.slots
            listMap = list.list.indices.map {
                it to slots.elementAt(it % slots.size)
            }.groupBy {
                it.first / slots.size
            }
        }

        fun manageDisplay(inventory: Inventory, list: MenuList<T>, page: Int) {
            val listSlots = listMap?.getValue(page)?.map {
                inventory.setItem(it.second, list.display(list.list[it.first]).item)
                it.second
            }?.toSet() ?: return
            val fillerSlots = list.slots - listSlots
            fillerSlots.forEach { inventory.setItem(it, list.filler.display.item) }
        }

        fun manageBehavior(event: InventoryClickEvent, list: MenuList<T>, page: Int) {
            if (event.slot !in list.slots) return
            event.isCancelled = true
            val action = ButtonAction[event.click, event.hotbarButton] ?: return
            if (event.currentItem?.type == list.filler.display.material) {
                list.filler.interactAction(Interacted(event, action))
                return
            }
            val index = listMap?.getValue(page)?.first { it.second == event.slot }?.first ?: return
            list.interactAction(Interacted(event, action), list.list[index])
        }
    }
}

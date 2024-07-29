package me.matin.core.managers.menu.items.other

import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.utils.DisplayItem
import org.bukkit.event.inventory.InventoryClickEvent

class Filler(val display: DisplayItem = DisplayItem(), val interactAction: Interacted.() -> Unit = {}) {

    fun manageBehavior(slots: Set<Int>, event: InventoryClickEvent) {
        if (event.slot !in slots) return
        event.isCancelled = true
        interactAction(Interacted(event, ButtonAction[event.click, event.hotbarButton] ?: return))
    }

    fun manageDisplay(menu: InventoryMenu, slots: Set<Int>) = slots.forEach {
        menu.inventory.setItem(it, display.toItem())
    }
}
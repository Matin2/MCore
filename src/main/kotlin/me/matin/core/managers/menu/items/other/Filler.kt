package me.matin.core.managers.menu.items.other

import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.utils.DisplayItem
import me.matin.core.managers.menu.utils.Interacted
import org.bukkit.event.inventory.InventoryClickEvent

class Filler(val display: DisplayItem = DisplayItem(), val interactAction: Interacted.() -> Unit = {}) {

    class Manager(private val menu: InventoryMenu, private val slots: Set<Int>) {

        fun manageBehavior(event: InventoryClickEvent) {
            if (event.slot !in slots) return
            event.isCancelled = true
            menu.filler.interactAction(Interacted(event, ButtonAction[event.click, event.hotbarButton] ?: return))
        }

        fun manageDisplay() = slots.forEach {
            menu.inventory.setItem(it, menu.filler.display.toItem())
        }
    }
}
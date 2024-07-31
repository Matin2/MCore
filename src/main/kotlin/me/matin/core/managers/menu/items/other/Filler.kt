package me.matin.core.managers.menu.items.other

import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.utils.DisplayItem
import me.matin.core.managers.menu.utils.Interacted
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class Filler(val display: DisplayItem = DisplayItem(), val interactAction: Interacted.() -> Unit = {}) {

    class Manager {

        fun manageBehavior(event: InventoryClickEvent, filler: Filler, slots: Set<Int>) {
            if (event.slot !in slots) return
            event.isCancelled = true
            filler.interactAction(Interacted(event, ButtonAction[event.click, event.hotbarButton] ?: return))
        }

        fun manageDisplay(inventory: Inventory, filler: Filler, slots: Set<Int>) = slots.forEach {
            inventory.setItem(it, filler.display.toItem())
        }
    }
}
package me.matin.core.managers.menu.items.button

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class ButtonManager {

    val buttons = mutableSetOf<Button>()

    fun manageBehavior(event: InventoryClickEvent) {
        val button = buttons.firstOrNull { event.slot in it.slots && it.show } ?: return
        event.isCancelled = true
        button.interactAction(button.Interacted(event, ButtonAction[event.click, event.hotbarButton] ?: return))
    }

    fun manageDisplay(inventory: Inventory, fillerSlots: MutableSet<Int>) {
        buttons.forEach { button ->
            if (!button.show) return
            button.inventory = inventory
            button.slots.forEach {
                inventory.setItem(it, button.statesDisplay[button.state].item)
                fillerSlots.remove(it)
            }
        }
    }
}
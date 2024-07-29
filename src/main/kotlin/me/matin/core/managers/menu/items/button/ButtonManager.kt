package me.matin.core.managers.menu.items.button

import me.matin.core.managers.menu.InventoryMenu
import org.bukkit.event.inventory.InventoryClickEvent

class ButtonManager(private val menu: InventoryMenu) {

    fun manageBehavior(event: InventoryClickEvent) {
        val button = menu.buttons.firstOrNull { event.slot in it.slots } ?: return
        event.isCancelled = true
        button.interactAction(button.Interacted(event, ButtonAction[event.click, event.hotbarButton] ?: return))
    }

    fun manageDisplay(fillerSlots: MutableSet<Int>) {
        menu.buttons.forEach { button ->
            if (!button.show) return
            button.menu = menu
            button.slots.forEach {
                menu.inventory.setItem(it, button.statesDisplay[button.state].toItem())
                fillerSlots.remove(it)
            }
        }
    }
}
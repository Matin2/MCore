package me.matin.core.managers.menu.items.button

import me.matin.core.managers.menu.Menu
import me.matin.core.managers.menu.items.SlotType
import org.bukkit.event.inventory.InventoryClickEvent

class ButtonManager(private val menu: Menu) {

    fun manageBehavior(event: InventoryClickEvent) {
        val button = menu.items.filterIsInstance<Button>().firstOrNull { event.slot in it.slots } ?: return
        event.isCancelled = true
        if (ButtonAction.entries.none { it.clickType == event.click }) return
        if (SlotType.entries.none { it.type == event.slotType }) return
        button.interactAction(button.Interacted(event))
    }

    fun manageDisplay() = menu.items.filterIsInstance<Button>().forEach { button ->
        button.menu = menu
        button.slots.forEach {
            menu.inventory.setItem(it, button.statesDisplay[button.state].toItem())
        }
    }
}
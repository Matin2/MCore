package me.matin.core.managers.menu.items.button

import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.menus.ListMenu
import me.matin.core.managers.menu.menus.Menu
import org.bukkit.event.inventory.InventoryClickEvent

class ButtonManager(private val menu: InventoryMenu) {

    fun manageBehavior(event: InventoryClickEvent) {
        val button = menu.buttons.firstOrNull { event.slot in it.slots } ?: return
        event.isCancelled = true
        if (ButtonAction.entries.none { it.clickType == event.click }) return
        button.interactAction(button.Interacted(event))
    }

    fun manageDisplay() {
        when (menu) {
            is Menu -> {
                menu.buttons.forEach { button ->
                    button.menu = menu
                    button.slots.forEach {
                        menu.inventory.setItem(it, button.statesDisplay[button.state].toItem())
                    }
                }
            }

            is ListMenu<*> -> {
                menu.buttonsMap.forEach { (button, pages) ->
                    if (pages != null && menu.page !in pages) return@forEach
                    button.menu = menu
                    button.slots.forEach {
                        menu.inventory.setItem(it, button.statesDisplay[button.state].toItem())
                    }
                }
            }
        }
    }
}
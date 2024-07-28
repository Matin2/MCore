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
            is Menu -> menu.buttons.forEach { display(it) }
            is ListMenu<*> -> menu.buttonsMap.filter { (_, pages) ->
                pages == null || menu.page !in pages
            }.forEach { display(it.key) }
        }
    }

    private fun display(button: Button) {
        if (!button.show) return
        button.menu = menu
        button.slots.forEach {
            menu.inventory.setItem(it, button.statesDisplay[button.state].toItem())
        }
    }
}
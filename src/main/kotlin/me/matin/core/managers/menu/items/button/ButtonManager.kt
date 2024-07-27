package me.matin.core.managers.menu.items.button

import me.matin.core.managers.TaskManager
import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.menus.ListMenu
import me.matin.core.managers.menu.menus.Menu
import org.bukkit.event.inventory.InventoryClickEvent

class ButtonManager(private val menu: InventoryMenu) {

    fun manageBehavior(event: InventoryClickEvent) = TaskManager.runTask(true) {
        val button = menu.buttons.firstOrNull { event.slot in it.slots } ?: return@runTask
        event.isCancelled = true
        if (ButtonAction.entries.none { it.clickType == event.click }) return@runTask
        TaskManager.runTask {
            button.interactAction(button.Interacted(event))
        }
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
                    if (pages?.contains(menu.page) != true) return@forEach
                    button.menu = menu
                    button.slots.forEach {
                        menu.inventory.setItem(it, button.statesDisplay[button.state].toItem())
                    }
                }
            }
        }
    }
}
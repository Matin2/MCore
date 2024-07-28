package me.matin.core.managers.menu.items

import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.utils.DisplayItem
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

data class Filler(val display: DisplayItem = DisplayItem(), val interactAction: Interacted.() -> Unit = {}) {

    fun manageBehavior(slots: Set<Int>, event: InventoryClickEvent) {
        if (event.slot !in slots) return
        event.isCancelled = true
        if (ButtonAction.entries.none { it.clickType == event.click }) return
        interactAction(Interacted(event))
    }

    fun manageDisplay(menu: InventoryMenu, slots: Set<Int>) = slots.forEach {
        menu.inventory.setItem(it, display.toItem())
    }
}

data class ListItem<T>(
    val slots: Set<Int>,
    val list: List<T>,
    val display: (T) -> DisplayItem,
    val interactAction: Interacted.(T) -> Unit = {}
)

@Suppress("unused", "MemberVisibilityCanBePrivate", "DEPRECATION")
class Interacted(private val event: InventoryClickEvent) {

    val view = event.view
    val slot = event.slot
    val action: ButtonAction by lazy {
        if (event.click == ClickType.NUMBER_KEY) ButtonAction.entries.first { it.hotbar == event.hotbarButton }
        else ButtonAction.entries.first { it.clickType == event.click }
    }
    var cursor: ItemStack = event.cursor
        set(value) {
            event.setCursor(value)
        }
}

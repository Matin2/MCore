package me.matin.core.managers.menu.items.other

import me.matin.core.managers.menu.items.button.ButtonAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

@Suppress("unused", "MemberVisibilityCanBePrivate", "DEPRECATION")
open class Interacted(private val event: InventoryClickEvent, val action: ButtonAction) {

    val view = event.view
    val slot = event.slot
    var cursor: ItemStack = event.cursor
        set(value) {
            event.setCursor(value)
        }
}

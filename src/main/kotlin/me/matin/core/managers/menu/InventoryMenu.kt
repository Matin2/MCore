package me.matin.core.managers.menu

import me.matin.core.managers.menu.items.other.Filler
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryHolder

abstract class InventoryMenu: InventoryHolder {

    abstract val title: Component
    abstract val type: MenuType
    abstract fun manageBehaviour(event: InventoryClickEvent)
    abstract fun close(closeInventory: Boolean = true)
    open val filler: Filler = Filler()
    open val freezeBottomInv: Boolean = false
    open val preventCursorLoss: Boolean = true
}
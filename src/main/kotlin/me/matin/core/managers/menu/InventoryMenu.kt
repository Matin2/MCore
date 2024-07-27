package me.matin.core.managers.menu

import me.matin.core.managers.menu.items.button.Button
import net.kyori.adventure.text.Component
import org.bukkit.inventory.InventoryHolder

abstract class InventoryMenu: InventoryHolder {

    abstract val title: Component
    abstract val type: MenuType
    abstract val buttons: MutableSet<Button>
    open val freezeBottomInv: Boolean = false
    open val preventCursorLoss: Boolean = true
}
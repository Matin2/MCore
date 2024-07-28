package me.matin.core.managers.menu

import me.matin.core.managers.menu.items.Filler
import me.matin.core.managers.menu.items.button.Button
import net.kyori.adventure.text.Component
import org.bukkit.inventory.InventoryHolder

abstract class InventoryMenu: InventoryHolder {

    abstract val title: Component
    abstract val type: MenuType
    open val buttons: MutableSet<Button> = mutableSetOf()
    open val filler: Filler = Filler()
    open val freezeBottomInv: Boolean = false
    open val preventCursorLoss: Boolean = true
}
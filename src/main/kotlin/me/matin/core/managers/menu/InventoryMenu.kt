package me.matin.core.managers.menu

import me.matin.core.managers.menu.items.button.Button
import net.kyori.adventure.text.Component
import org.bukkit.inventory.InventoryHolder

interface InventoryMenu: InventoryHolder {

    val title: Component
    val type: MenuType
    val buttons: MutableSet<Button>
    val freezeBottomInv: Boolean
    val preventCursorLoss: Boolean
}
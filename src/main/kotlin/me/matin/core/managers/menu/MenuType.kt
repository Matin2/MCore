package me.matin.core.managers.menu

import org.bukkit.event.inventory.InventoryType
import org.jetbrains.annotations.Range

@Suppress("unused")
sealed class MenuType private constructor(val type: InventoryType?, open val rows: Int?) {

    data class NORMAL(override val rows: @Range(from = 1, to = 6) Int): MenuType(null, rows)
    data object ANVIL: MenuType(InventoryType.ANVIL, null)
    data object BREWING: MenuType(InventoryType.BREWING, null)
    data object DISPENSER: MenuType(InventoryType.DISPENSER, null)
    data object FURNACE: MenuType(InventoryType.FURNACE, null)
    data object GRINDSTONE: MenuType(InventoryType.GRINDSTONE, null)
    data object HOPPER: MenuType(InventoryType.HOPPER, null)
    data object SMITHING: MenuType(InventoryType.SMITHING, null)
    data object WORKBENCH: MenuType(InventoryType.WORKBENCH, null)

    companion object {

        @JvmStatic
        infix operator fun get(type: InventoryType): MenuType = when (type) {
            InventoryType.ANVIL -> ANVIL
            InventoryType.BREWING -> BREWING
            InventoryType.DISPENSER -> DISPENSER
            InventoryType.FURNACE -> FURNACE
            InventoryType.GRINDSTONE -> GRINDSTONE
            InventoryType.HOPPER -> HOPPER
            InventoryType.SMITHING -> SMITHING
            InventoryType.WORKBENCH -> WORKBENCH
            else -> NORMAL(3)
        }
    }
}
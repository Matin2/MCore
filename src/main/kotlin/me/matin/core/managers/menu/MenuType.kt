package me.matin.core.managers.menu

import org.bukkit.event.inventory.InventoryType

@Suppress("unused")
enum class MenuType(val type: InventoryType?, val rows: Int?) {
    ONE(null, 1),
    TWO(null, 2),
    THREE(null, 3),
    FOUR(null, 4),
    FIVE(null, 5),
    SIX(null, 6),
    ANVIL(InventoryType.ANVIL, null),
    BEACON(InventoryType.BEACON, null),
    BREWING(InventoryType.BREWING, null),
    CARTOGRAPHY(InventoryType.CARTOGRAPHY, null),
    DISPENSER(InventoryType.DISPENSER, null),
    ENCHANTING(InventoryType.ENCHANTING, null),
    FURNACE(InventoryType.FURNACE, null),
    GRINDSTONE(InventoryType.GRINDSTONE, null),
    HOPPER(InventoryType.HOPPER, null),
    LOOM(InventoryType.LOOM, null),
    MERCHANT(InventoryType.MERCHANT, null),
    SMITHING(InventoryType.SMITHING, null),
    STONECUTTER(InventoryType.STONECUTTER, null),
    WORKBENCH(InventoryType.WORKBENCH, null),
}
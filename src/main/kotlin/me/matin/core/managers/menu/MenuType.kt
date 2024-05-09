package me.matin.core.managers.menu

import org.bukkit.event.inventory.InventoryType

@Suppress("unused")
enum class MenuType(val type: InventoryType) {
    NORMAL(InventoryType.CHEST),
    ANVIL(InventoryType.ANVIL),
    BEACON(InventoryType.BEACON),
    BREWING(InventoryType.BREWING),
    CARTOGRAPHY(InventoryType.CARTOGRAPHY),
    DISPENSER(InventoryType.DISPENSER),
    ENCHANTING(InventoryType.ENCHANTING),
    FURNACE(InventoryType.FURNACE),
    GRINDSTONE(InventoryType.GRINDSTONE),
    HOPPER(InventoryType.HOPPER),
    LOOM(InventoryType.LOOM),
    MERCHANT(InventoryType.MERCHANT),
    SMITHING(InventoryType.SMITHING),
    STONECUTTER(InventoryType.STONECUTTER),
    WORKBENCH(InventoryType.WORKBENCH),
}
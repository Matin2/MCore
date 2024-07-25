package me.matin.core.managers.menu.items

import org.bukkit.event.inventory.InventoryType

enum class SlotType(val type: InventoryType.SlotType) {

    CONTAINER(InventoryType.SlotType.CONTAINER),
    CRAFTING(InventoryType.SlotType.CRAFTING),
    FUEL(InventoryType.SlotType.FUEL),
    RESULT(InventoryType.SlotType.RESULT),
}

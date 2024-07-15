package me.matin.core.managers.menu

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class Button(vararg val items: Pair<ItemStack, Int>, val clickAction: (event: InventoryClickEvent) -> Unit)
package me.matin.core.managers.item

import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("unused")
object ItemManager {

    @JvmStatic
    fun ItemStack?.checkAir(): ItemStack? = takeUnless { it?.isEmpty == true }

    @JvmStatic
    fun drop(item: ItemStack, location: Location, blockFace: BlockFace? = null) {
        if (item.isEmpty) return
        val dropLocation = blockFace?.let { location.block.getRelative(it).location } ?: location
        location.world.dropItemNaturally(dropLocation, item)
    }

    @JvmStatic
    fun getSlots(player: Player, slots: String): Set<Int> = slots.split(',').filter { it.isNotBlank() }.map {
        when (it) {
            "mainhand" -> player.inventory.heldItemSlot
            "offhand" -> 40
            "helmet" -> 36
            "chestplate" -> 37
            "leggings" -> 38
            "boots" -> 39
            else -> it.toIntOrNull() ?: return emptySet()
        }
    }.toSet()
}
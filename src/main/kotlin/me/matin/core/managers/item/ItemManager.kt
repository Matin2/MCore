package me.matin.core.managers.item

import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@Suppress("unused")
object ItemManager {

    @JvmStatic
    fun ItemStack?.checkAir(): ItemStack? = this?.takeUnless { it.isEmpty }

    @JvmStatic
    fun drop(item: ItemStack, location: Location, blockFace: Optional<BlockFace> = Optional.empty()) {
        if (item.isEmpty) return
        val dropLocation = if (blockFace.isPresent) location.block.getRelative(blockFace.get()).location else location
        location.world.dropItemNaturally(dropLocation, item)
    }

    @JvmStatic
    fun getSlots(player: Player, slots: String): Set<Int> {
        return slots.split(',').filter { it.isNotBlank() }.map {
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
}
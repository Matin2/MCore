package me.matin.core.managers.item

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("unused")
object ItemManager {

    @JvmStatic
    fun drop(item: ItemStack, x: Int, y: Int, z: Int, world: World, blockFace: BlockFace) {
        if (item.type == Material.AIR || item.amount == 0) return
        world.dropItemNaturally(world.getBlockAt(x, y, z).getRelative(blockFace).location, item)
    }

    @JvmStatic
    fun drop(item: ItemStack, location: Location, blockFace: BlockFace) {
        if (item.type == Material.AIR || item.amount == 0) return
        location.world.dropItemNaturally(location.block.getRelative(blockFace).location, item)
    }

    @JvmStatic
    fun drop(item: ItemStack, x: Int, y: Int, z: Int, world: World) {
        if (item.type == Material.AIR || item.amount == 0) return
        world.dropItemNaturally(world.getBlockAt(x, y, z).location, item)
    }

    @JvmStatic
    fun drop(item: ItemStack, location: Location) {
        if (item.type == Material.AIR || item.amount == 0) return
        location.world.dropItemNaturally(location, item)
    }

    operator fun get(player: Player, slots: String): Set<Int> {
        val result= emptySet<Int>() as MutableSet<Int>
        slots.split(',').filter { it.isNotBlank() }.forEach {
            when (it) {
                "mainhand" -> result.add(player.inventory.heldItemSlot)
                "offhand" -> result.add(40)
                "helmet" -> result.add(36)
                "chestplate" -> result.add(37)
                "leggings" -> result.add(38)
                "boots" -> result.add(39)
                else -> it.toIntOrNull()?.let { slot -> result.add(slot) }
            }
        }
        return result
    }
}
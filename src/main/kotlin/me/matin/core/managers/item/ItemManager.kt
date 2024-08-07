package me.matin.core.managers.item

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("unused")
object ItemManager {

    val ItemStack?.isAir get() = this?.takeUnless { it.amount == 0 || it.type == Material.AIR } == null
    val air get() = ItemStack(Material.AIR)

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
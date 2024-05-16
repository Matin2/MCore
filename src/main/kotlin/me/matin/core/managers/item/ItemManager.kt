package me.matin.core.managers.item

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import kotlin.math.min

@Suppress("unused")
object ItemManager {

    @JvmStatic
    fun modify(type: ItemModifyType, modificationType: ItemModificationType, modificationAmount: Int, player: Player, slots: ArrayList<Int>) {
        for (slot in slots) {
            if (slot !in 0..40) continue
            when (type) {
                ItemModifyType.AMOUNT -> modifyAmount(modificationType, modificationAmount, player, slot)
                ItemModifyType.DURABILITY -> modifyDurability(modificationType, modificationAmount, player, slot)
            }
        }
    }

    @JvmStatic
    fun modify(type: ItemModifyType, modificationType: ItemModificationType, modificationAmount: Int, player: Player, slot: Int) {
        when (type) {
            ItemModifyType.AMOUNT -> modifyAmount(modificationType, modificationAmount, player, slot)
            ItemModifyType.DURABILITY -> modifyDurability(modificationType, modificationAmount, player, slot)
        }
    }

    private fun modifyAmount(modification: ItemModificationType, amount: Int, player: Player, slot: Int) {
        val item = player.inventory.getItem(slot) ?: return
        if (item.type == Material.AIR) return
        val newAmount = when (modification) {
            ItemModificationType.SET -> amount
            ItemModificationType.ADD -> item.amount + amount
            ItemModificationType.TAKE -> item.amount - amount
        }
        if (newAmount == 0) return
        item.amount = min(newAmount, item.maxStackSize)
    }

    private fun modifyDurability(modification: ItemModificationType, amount: Int, player: Player, slot: Int) {
        val item = player.inventory.getItem(slot) ?: return
        if (item.type == Material.AIR || item.itemMeta.isUnbreakable) return
        val damageable = item.itemMeta as? Damageable ?: return
        val damage = when (modification) {
            ItemModificationType.SET -> item.type.maxDurability - amount
            ItemModificationType.ADD -> damageable.damage - amount
            ItemModificationType.TAKE -> damageable.damage + amount
        }
        if (damage == 0) return
        damageable.damage = damage
        item.setItemMeta(damageable)
    }

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
    fun getSlots(player: Player, slots: String): ArrayList<Int> {
        val result = ArrayList<Int>()
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
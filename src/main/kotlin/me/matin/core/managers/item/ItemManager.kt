package me.matin.core.managers.item

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

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
        var mod: Int
        val item = player.inventory.getItem(slot) ?: return
        if (item.type == Material.AIR) return
        val itemAmount = item.amount
        val itemMaxAmount = item.maxStackSize
        mod = when (modification) {
            ItemModificationType.SET -> amount
            ItemModificationType.ADD -> itemAmount + amount
            ItemModificationType.TAKE -> itemAmount - amount
        }
        if (mod > itemMaxAmount) mod = itemMaxAmount
        if (itemAmount != 0) {
            item.amount = mod
        }
    }

    private fun modifyDurability(modification: ItemModificationType, amount: Int, player: Player, slot: Int) {
        val item = player.inventory.getItem(slot) ?: return
        if (item.type == Material.AIR || item.itemMeta.isUnbreakable) return
        val damageable = item.itemMeta as? Damageable ?: return
        val itemMaxDurability = item.type.maxDurability.toInt()
        val mod: Int
        val itemDamage = if (damageable.hasDamage()) damageable.damage else 0
        mod = when (modification) {
            ItemModificationType.SET -> itemMaxDurability - amount
            ItemModificationType.ADD -> itemDamage - amount
            ItemModificationType.TAKE -> itemDamage + amount
        }
        if (mod == 0 || item.type == Material.AIR) return
        damageable.damage = mod
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("MCore")!!, Runnable {
            item.setItemMeta(
                damageable
            )
        })
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
package me.matin.core.function.item

import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class Item {

    companion object {

        @JvmStatic
        fun modify(type: ItemModifyType, modificationType: ItemModificationType, modificationAmount: Int, player: Player, slots: ArrayList<Int>) {
            for (slot in slots) {
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
            val itemAmount = if (item.type == Material.AIR) 0 else item.amount
            val itemMaxAmount = if (item.type == Material.AIR) 0 else item.maxStackSize
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
            if (item.type != Material.AIR && !item.itemMeta.isUnbreakable && item.itemMeta is Damageable) {
                val damageable = item.itemMeta as Damageable
                val itemMaxDurability = item.type.maxDurability.toInt()
                val mod: Int
                val itemDamage = if (damageable.hasDamage()) damageable.damage else 0
                mod = when (modification) {
                    ItemModificationType.SET -> itemMaxDurability - amount
                    ItemModificationType.ADD -> itemDamage - amount
                    ItemModificationType.TAKE -> itemDamage + amount
                }
                if (mod != 0 && item.type != Material.AIR) {
                    damageable.damage = mod
                    Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, Runnable {
                        item.setItemMeta(
                            damageable
                        )
                    })
                }
            }
        }

        @JvmStatic
        fun drop(item: ItemStack, x: Int, y: Int, z: Int, world: World, blockFace: BlockFace) {
            if (item.type != Material.AIR && item.amount != 0) {
                val block = world.getBlockAt(x, y, z)
                world.dropItemNaturally(block.getRelative(blockFace).location, item)
            }
        }

        @JvmStatic
        fun drop(item: ItemStack, location: Location, blockFace: BlockFace) {
            if (item.type != Material.AIR && item.amount != 0) {
                val block = location.block
                location.world.dropItemNaturally(block.getRelative(blockFace).location, item)
            }
        }

        @JvmStatic
        fun drop(item: ItemStack, x: Int, y: Int, z: Int, world: World) {
            if (item.type != Material.AIR && item.amount != 0) {
                val block = world.getBlockAt(x, y, z)
                world.dropItemNaturally(block.location, item)
            }
        }

        @JvmStatic
        fun drop(item: ItemStack, location: Location) {
            if (item.type != Material.AIR && item.amount != 0) {
                location.world.dropItemNaturally(location, item)
            }
        }

        @JvmStatic
        fun getSlots(player: Player, slots: String): ArrayList<Int> {
            val slotsArray = slots.split(',').dropLastWhile { it.isEmpty() }.toTypedArray()
            val result: ArrayList<Int> = ArrayList()
            if (slotsArray.isNotEmpty()) {
                for (slot in slotsArray) {
                    val presetSlot = getPresetSlot(player, slot) ?: continue
                    result.add(presetSlot)
                }
            }
            return result
        }

        private fun getPresetSlot(p: Player, slot: String): Int? {
            var result: Int? = slot.toIntOrNull()
            when (slot) {
                "mainhand", "main" -> result = p.inventory.heldItemSlot
                "offhand", "off" -> result = 40
                "helmet", "helm" -> result = 39
                "chestplate", "chest" -> result = 38
                "leggings", "leggs" -> result = 37
                "boots" -> result = 36
            }
            return result
        }
    }
}
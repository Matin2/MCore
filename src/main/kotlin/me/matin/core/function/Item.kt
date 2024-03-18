package me.matin.core.function

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

    enum class Modification {
        SET,
        ADD,
        TAKE
    }

    enum class ModifyType {
        DURABILITY,
        AMOUNT,
    }

    companion object {

        private var plugin = Core()

        @JvmStatic
        fun modify(modification: Modification, amount: Int, player: Player, slots: ArrayList<Int>, type: ModifyType) {
            for (slot in slots) {
                when (type) {
                    ModifyType.AMOUNT -> modifyAmount(modification, amount, player, slot)
                    ModifyType.DURABILITY -> modifyDurability(modification, amount, player, slot)
                }
            }
        }

        @JvmStatic
        fun modify(modification: Modification, amount: Int, player: Player, slot: Int, type: ModifyType) {
            when (type) {
                ModifyType.AMOUNT -> modifyAmount(modification, amount, player, slot)
                ModifyType.DURABILITY -> modifyDurability(modification, amount, player, slot)
            }
        }

        private fun modifyAmount(modification: Modification, amount: Int, player: Player, slot: Int) {
            var mod: Int
            val item = player.inventory.getItem(slot) ?: return
            val itemAmount = if (item.type == Material.AIR) 0 else item.amount
            val itemMaxAmount = if (item.type == Material.AIR) 0 else item.maxStackSize
            mod = when (modification) {
                Modification.SET -> amount
                Modification.ADD -> itemAmount + amount
                Modification.TAKE -> itemAmount - amount
            }
            if (mod > itemMaxAmount) mod = itemMaxAmount
            if (itemAmount != 0) {
                item.amount = mod
            }
        }

        private fun modifyDurability(modification: Modification, amount: Int, player: Player, slot: Int) {
            val item = player.inventory.getItem(slot) ?: return
            if (item.type != Material.AIR && !item.itemMeta.isUnbreakable && item.itemMeta is Damageable) {
                val damageable = item.itemMeta as Damageable
                val itemMaxDurability = item.type.maxDurability.toInt()
                val mod: Int
                val itemDamage = if (damageable.hasDamage()) damageable.damage else 0
                mod = when (modification) {
                    Modification.SET -> itemMaxDurability - amount
                    Modification.ADD -> itemDamage - amount
                    Modification.TAKE -> itemDamage + amount
                }
                if (mod != 0 && item.type != Material.AIR) {
                    damageable.damage = mod
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
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
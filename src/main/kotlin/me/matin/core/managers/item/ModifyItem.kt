package me.matin.core.managers.item

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import kotlin.math.min

@Suppress("unused")
enum class ModifyItem {
    AMOUNT,
    DURABILITY;

    private fun modifyAmount(item: ItemStack, modification: ItemModifyType, value: Int) {
        item.takeUnless { it.type == Material.AIR }?.apply {
            amount = min(when (modification) {
                ItemModifyType.SET -> value
                ItemModifyType.ADD -> amount + value
                ItemModifyType.TAKE -> amount - value
            }, item.maxStackSize)
        } ?: return
    }
    private fun modifyDurability(item: ItemStack, modification: ItemModifyType, value: Int) {
        item.itemMeta = (item.takeUnless { it.type == Material.AIR || it.itemMeta.isUnbreakable }?.itemMeta as? Damageable)?.apply {
            damage = when (modification) {
                ItemModifyType.SET -> item.type.maxDurability - value
                ItemModifyType.ADD -> damage - value
                ItemModifyType.TAKE -> damage + value
            }
        } ?: return
    }

    class Item internal constructor(private val item: ItemStack?, private val type: ModifyItem) {
        operator fun divAssign(amount: Int) {
            val item = this.item ?: return
            val modifyType = amount.takeUnless { it == 0 }?.let { if (it > 0) ItemModifyType.ADD else ItemModifyType.TAKE } ?: return
            when (type) {
                AMOUNT -> AMOUNT.modifyAmount(item, modifyType, amount)
                DURABILITY -> DURABILITY.modifyDurability(item, modifyType, amount)
            }
        }
    }
    class Items internal constructor(private val items: List<ItemStack>, private val type: ModifyItem) {
        operator fun divAssign(amount: Int) {
            items.ifEmpty { return }
            val modifyType = amount.takeUnless { it == 0 }?.let { if (it > 0) ItemModifyType.ADD else ItemModifyType.TAKE } ?: return
            items.forEach {
                when (type) {
                    AMOUNT -> AMOUNT.modifyAmount(it, modifyType, amount)
                    DURABILITY -> DURABILITY.modifyDurability(it, modifyType, amount)
                }
            }
        }
    }

    operator fun get(item: ItemStack): Item = Item(item, this)
    operator fun get(player: Player, slot: Int): Item {
        return Item(player.inventory.getItem(slot), this)
    }
    operator fun get(items: List<ItemStack>): Items = Items(items, this)
    operator fun get(player: Player, slots: Set<Int>): Items {
        val items = slots.filter {
            it in 0..40 && player.inventory.getItem(it) != null
        }.map { player.inventory.getItem(it)!! }
        return Items(items, this)
    }

    operator fun set(item: ItemStack, modifyType: ItemModifyType = ItemModifyType.SET, amount: Int) {
        when (this) {
            AMOUNT -> modifyAmount(item, modifyType, amount)
            DURABILITY -> modifyDurability(item, modifyType, amount)
        }
    }
    operator fun set(player: Player, slot: Int, modifyType: ItemModifyType = ItemModifyType.SET, amount: Int) {
        val item = player.inventory.getItem(slot) ?: return
        when (this) {
            AMOUNT -> modifyAmount(item, modifyType, amount)
            DURABILITY -> modifyDurability(item, modifyType, amount)
        }
    }
    operator fun set(player: Player, slots: Set<Int>, modifyType: ItemModifyType = ItemModifyType.SET, amount: Int) {
        slots.filter { it in 0..40 }.forEach {
            val item = player.inventory.getItem(it) ?: return
            when (this) {
                AMOUNT -> modifyAmount(item, modifyType, amount)
                DURABILITY -> modifyDurability(item, modifyType, amount)
            }
        }
    }
}
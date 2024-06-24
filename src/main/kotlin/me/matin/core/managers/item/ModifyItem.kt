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

    class Item internal constructor(internal val item: ItemStack, internal val type: ModifyItem)

    operator fun get(item: ItemStack): Item = Item(item, this)
    operator fun get(player: Player, slot: Int): Item? {
        return Item(player.inventory.getItem(slot) ?: return null, this)
    }
    operator fun get(player: Player, slots: Set<Int>): List<Item>? {
        return slots.filter { it in 0..40 }.map {
            Item(player.inventory.getItem(it) ?: return null, this)
        }
    }

    operator fun Item?.divAssign(amount: Int) {
        this ?: return
        val modifyType = amount.takeUnless { it == 0 }?.let { if (it > 0) ItemModifyType.ADD else ItemModifyType.TAKE } ?: return
        when (type) {
            AMOUNT -> modifyAmount(this.item, modifyType, amount)
            DURABILITY -> modifyDurability(this.item, modifyType, amount)
        }
    }
    operator fun List<Item>?.divAssign(amount: Int) {
        this?.forEach { it /= amount } ?: return
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
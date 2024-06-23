package me.matin.core.managers.item

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable
import kotlin.math.min

@Suppress("unused")
enum class ModifyItem {
    AMOUNT,
    DURABILITY;

    private fun modifyAmount(modification: ItemModifyType, amount: Int, player: Player, slot: Int) {
        val item = player.inventory.getItem(slot)?.takeUnless { it.type == Material.AIR } ?: return
        val newAmount = when (modification) {
            ItemModifyType.SET -> amount
            ItemModifyType.ADD -> item.amount + amount
            ItemModifyType.TAKE -> item.amount - amount
        }.takeUnless { it == 0 } ?: return
        item.amount = min(newAmount, item.maxStackSize)
    }

    private fun modifyDurability(modification: ItemModifyType, amount: Int, player: Player, slot: Int) {
        val item = player.inventory.getItem(slot)?.takeUnless { it.type == Material.AIR || it.itemMeta.isUnbreakable } ?: return
        val damageable = item.itemMeta as? Damageable ?: return
        val damage = when (modification) {
            ItemModifyType.SET -> item.type.maxDurability - amount
            ItemModifyType.ADD -> damageable.damage - amount
            ItemModifyType.TAKE -> damageable.damage + amount
        }.takeUnless { it == 0 } ?: return
        damageable.damage = damage
        item.setItemMeta(damageable)
    }

    class Item internal constructor(
        private val type: ModifyItem,
        private val player: Player,
        private val slots: Set<Int>
    ) {
        internal constructor(type: ModifyItem, player: Player, slot: Int): this(
            type,
            player,
            setOf(slot)
        )

        operator fun plusAssign(amount: Int) {
            for (slot in slots) {
                if (slot !in 0..40) continue
                when (type) {
                    AMOUNT -> AMOUNT.modifyAmount(ItemModifyType.ADD, amount, player, slot)
                    DURABILITY -> DURABILITY.modifyDurability(ItemModifyType.ADD, amount, player, slot)
                }
            }
        }

        operator fun minusAssign(amount: Int) {
            for (slot in slots) {
                if (slot !in 0..40) continue
                when (type) {
                    AMOUNT -> AMOUNT.modifyAmount(ItemModifyType.TAKE, amount, player, slot)
                    DURABILITY -> AMOUNT.modifyDurability(ItemModifyType.TAKE, amount, player, slot)
                }
            }
        }
    }

    operator fun get(player: Player, slot: Int): Item = Item(this, player, slot)

    operator fun get(player: Player, slots: Set<Int>): Item = Item(this, player, slots)

    operator fun set(player: Player, slot: Int, modifyType: ItemModifyType = ItemModifyType.SET, amount: Int) {
        when (this) {
            AMOUNT -> modifyAmount(modifyType, amount, player, slot)
            DURABILITY -> modifyDurability(modifyType, amount, player, slot)
        }
    }

    operator fun set(player: Player, slots: Set<Int>, modifyType: ItemModifyType = ItemModifyType.SET, amount: Int) {
        for (slot in slots) {
            if (slot !in 0..40) continue
            when (this) {
                AMOUNT -> modifyAmount(modifyType, amount, player, slot)
                DURABILITY -> modifyDurability(modifyType, amount, player, slot)
            }
        }
    }
}
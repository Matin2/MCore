package me.matin.core.managers.menu.utils

import com.destroystokyo.paper.profile.PlayerProfile
import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.NBTContainer
import me.matin.core.managers.item.BannerOptions
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.inventory.meta.trim.ArmorTrim
import org.bukkit.potion.PotionType
import org.jetbrains.annotations.Range

@Suppress("unused")
data class DisplayItem(
    var material: Material = Material.AIR,
    var name: Component? = null,
    var lore: MutableList<Component> = mutableListOf(),
    var model: Int = -1,
    var glow: Boolean = false,
    var amount: @Range(from = 1, to = 99) Int = 1,
    var rarity: ItemRarity? = null,
    var hideTooltip: Boolean = false,
    var color: Color? = null,
    var head: PlayerProfile? = null,
    var trim: ArmorTrim? = null,
    var banner: BannerOptions? = null,
    var durability: Int = -1,
    var nbt: NBTContainer? = null
) {

    fun toItem(): ItemStack {
        if (material == Material.AIR) return ItemStack(Material.AIR)
        val item = ItemStack(material)
        val meta = item.itemMeta!!
        meta.setMaxStackSize(99)
        if (rarity != null) meta.setRarity(rarity)
        if (name != null) meta.itemName(name)
        item.amount = maxOf(minOf(amount, 99), 1)
        if (lore.isNotEmpty()) meta.lore(lore)
        if (model > 0) meta.setCustomModelData(model)
        meta.setEnchantmentGlintOverride(glow)
        meta.isHideTooltip = hideTooltip
        setColor(meta)
        setSkull(meta)
        setDurability(item, meta)
        trim?.also {
            (meta as? ArmorMeta)?.trim = trim
        }
        banner?.apply {
            setOptions(meta as? BannerMeta ?: return@apply)
        }
        meta.addItemFlags(*ItemFlag.entries.toTypedArray())
        item.itemMeta = meta
        nbt?.also {
            NBT.modify(item) { itemNBT ->
                itemNBT.mergeCompound(it)
            }
        }
        return item
    }

    private fun setColor(meta: ItemMeta) {
        when (meta) {
            is LeatherArmorMeta -> meta.setColor(color)
            is PotionMeta -> {
                meta.basePotionType = PotionType.REGENERATION
                meta.color = color
            }

            is FireworkEffectMeta -> meta.effect = FireworkEffect.builder().withColor(color ?: return).build()
        }
    }

    private fun setDurability(item: ItemStack, meta: ItemMeta) {
        durability.takeIf { it >= 0 }?.let {
            val health = (item.type.maxDurability * durability) / 100
            (meta as? Damageable)?.damage = item.type.maxDurability - health
        }
    }

    private fun setSkull(meta: ItemMeta) {
        (meta as? SkullMeta)?.playerProfile = (head ?: return)
    }

    companion object {

        @JvmStatic
        fun fromItem(item: ItemStack): DisplayItem {
            val meta =
                item.itemMeta?.takeIf { item.hasItemMeta() } ?: return DisplayItem(item.type, amount = item.amount)
            val name = meta.displayName() ?: meta.itemName()
            val lore: MutableList<Component> = meta.lore().takeIf { meta.hasLore() } ?: mutableListOf()
            val glow = if (meta.hasEnchantmentGlintOverride()) meta.enchantmentGlintOverride else meta.hasEnchants()
            val rarity = meta.rarity.takeIf { meta.hasRarity() }
            val trim = (meta as? ArmorMeta)?.trim
            val bannerOptions = BannerOptions.takeIf { meta is BannerMeta }?.getOptions(meta as BannerMeta)
            val nbt = NBT.itemStackToNBT(item) as? NBTContainer
            return DisplayItem(
                item.type,
                name,
                lore,
                meta.customModelData,
                glow,
                item.amount,
                rarity,
                meta.isHideTooltip,
                getColor(meta),
                getSkull(meta),
                trim,
                bannerOptions,
                getDurability(item, meta),
                nbt
            )
        }

        private fun getColor(meta: ItemMeta): Color? {
            return when (meta) {
                is LeatherArmorMeta -> meta.color
                is PotionMeta -> meta.color
                is FireworkEffectMeta -> meta.effect?.colors?.first()
                else -> null
            }
        }

        private fun getDurability(item: ItemStack, meta: ItemMeta): Int {
            val damage = (meta as? Damageable)?.damage
            return if (damage != null) (item.type.maxDurability * damage) / 100 else -1
        }

        private fun getSkull(meta: ItemMeta): PlayerProfile? = (meta as? SkullMeta)?.playerProfile
    }
}
package me.matin.core.managers.item

import org.bukkit.DyeColor
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.inventory.meta.BannerMeta

@Suppress("unused")
class BannerOptions(private vararg val patterns: Pattern) {

    constructor(vararg patterns: Pair<DyeColor, PatternType>): this(patterns = patterns.map {
        Pattern(
            it.first,
            it.second
        )
    }.toTypedArray())

    fun setOptions(meta: BannerMeta) {
        meta.patterns = patterns.toList()
    }

    companion object {

        @JvmStatic
        fun getOptions(meta: BannerMeta): BannerOptions =
            BannerOptions(*meta.patterns.map { it.color to it.pattern }.toTypedArray())
    }
}
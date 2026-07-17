package com.github.matin2.mcore.managers.search_menu

import com.github.matin2.mcore.methods.utils.component.component
import com.github.matin2.mcore.methods.utils.set
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.inventory.ItemStack
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketItem

@Suppress("NOTHING_TO_INLINE", "UnstableApiUsage")
internal class SearchMenuButtons {
	companion {
		val placeholder: PacketItem = ItemStack.of(PAPER).let {
			it[DataComponentTypes.ITEM_NAME] = component()
			it[DataComponentTypes.TOOLTIP_DISPLAY] = TooltipDisplay.tooltipDisplay().hideTooltip(true)
			SpigotConversionUtil.fromBukkitItemStack(it)
		}
		
		val pageUp: PacketItem = ItemStack.of(ARROW).let {
			it[DataComponentTypes.ITEM_NAME] = component("Next Page")
			SpigotConversionUtil.fromBukkitItemStack(it)
		}
		val pageDown: PacketItem = ItemStack.of(ARROW).let {
			it[DataComponentTypes.ITEM_NAME] = component("Previous Page")
			SpigotConversionUtil.fromBukkitItemStack(it)
		}
		
		val close: PacketItem = ItemStack.of(BARRIER).let {
			it[DataComponentTypes.ITEM_NAME] = component("Close", NamedTextColor.DARK_RED, BOLD)
			SpigotConversionUtil.fromBukkitItemStack(it)
		}
	}
}

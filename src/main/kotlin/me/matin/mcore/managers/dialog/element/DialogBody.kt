package me.matin.mcore.managers.dialog.element

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import io.papermc.paper.registry.data.dialog.body.DialogBody as PaperBody

@Suppress("unused")
sealed class DialogBody(val value: PaperBody): DialogElement {
	
	data class Message(val message: Component, val width: Int = 200): DialogBody(PaperBody.plainMessage(message, width))
	data class Item(
		val item: ItemStack,
		val description: Component,
		val descriptionWidth: Int = 200,
		val showDecoration: Boolean = true,
		val showTooltip: Boolean = true,
		val width: Int = 16,
		val height: Int = 16,
	): DialogBody(
		PaperBody.item(
			item, PaperBody.plainMessage(description, descriptionWidth),
			showDecoration, showTooltip, width, height
		)
	)
}

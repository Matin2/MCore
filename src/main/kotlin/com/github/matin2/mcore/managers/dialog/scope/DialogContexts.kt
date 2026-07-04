@file:Suppress("UnstableApiUsage")

package com.github.matin2.mcore.managers.dialog.scope

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.*
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.text.Component

class NoticeDialogContext(title: Component) : DialogContext(title) {
	
	lateinit var button: ActionButton
	
	override val type: NoticeType get() = DialogType.notice(button)
}

class ConfirmationDialogContext(title: Component) : DialogContext(title) {
	
	lateinit var yesButton: ActionButton
	lateinit var noButton: ActionButton
	
	override val type: ConfirmationType get() = DialogType.confirmation(yesButton, noButton)
}

class ServerLinksDialogContext(title: Component) : DialogContext(title) {
	
	lateinit var exitButton: ActionButton
	var columns: Int = 2
	var buttonWidth: Int = 150
	
	override val type: ServerLinksType
		get() = DialogType.serverLinks(exitButton, columns, buttonWidth)
}

class ListDialogContext(title: Component) : DialogContext(title) {
	
	val dialogs: MutableList<Dialog> = []
	lateinit var exitButton: ActionButton
	var columns: Int = 2
	var buttonWidth: Int = 150
	
	override val type: DialogListType
		get() = DialogType.dialogList(
			RegistrySet.valueSet(RegistryKey.DIALOG, dialogs),
			exitButton, columns, buttonWidth
		)
}

class MultiActionDialogContext(title: Component) : DialogContext(title) {
	
	val buttons: MutableList<ActionButton> = []
	lateinit var exitButton: ActionButton
	var columns: Int = 2
	
	override val type: MultiActionType get() = DialogType.multiAction(buttons, exitButton, columns)
}

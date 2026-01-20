@file:Suppress("UnstableApiUsage")

package me.matin.mcore.managers.dialog.scope

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.*
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.text.Component

class NoticeDialogScope(title: Component) : DialogScope(title) {
	
	lateinit var button: ActionButton
	
	override val type: NoticeType get() = DialogType.notice(button)
}

class ConfirmationDialogScope(title: Component) : DialogScope(title) {
	
	lateinit var yesButton: ActionButton
	lateinit var noButton: ActionButton
	
	override val type: ConfirmationType get() = DialogType.confirmation(yesButton, noButton)
}

class ServerLinksDialogScope(title: Component) : DialogScope(title) {
	
	lateinit var exitButton: ActionButton
	var columns: Int = 2
	var buttonWidth: Int = 150
	
	override val type: ServerLinksType
		get() = DialogType.serverLinks(exitButton, columns, buttonWidth)
}

class ListDialogScope(title: Component) : DialogScope(title) {
	
	val dialogs: MutableList<Dialog> = mutableListOf()
	lateinit var exitButton: ActionButton
	var columns: Int = 2
	var buttonWidth: Int = 150
	
	override val type: DialogListType
		get() = DialogType.dialogList(
			RegistrySet.valueSet(RegistryKey.DIALOG, dialogs),
			exitButton, columns, buttonWidth
		)
}

class MultiActionDialogScope(title: Component) : DialogScope(title) {
	
	val buttons: MutableList<ActionButton> = mutableListOf()
	lateinit var exitButton: ActionButton
	var columns: Int = 2
	
	override val type: MultiActionType get() = DialogType.multiAction(buttons, exitButton, columns)
}

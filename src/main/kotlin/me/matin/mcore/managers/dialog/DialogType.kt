package me.matin.mcore.managers.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.set.RegistrySet
import io.papermc.paper.registry.data.dialog.type.DialogType as PaperDialogType

@Suppress("unused")
sealed class DialogType(internal val paperType: PaperDialogType) {
	
	data class Notice(val button: DialogButton): DialogType(PaperDialogType.notice(button.paperButton))
	
	data class Confirmation(val yesButton: DialogButton, val noButton: DialogButton):
		DialogType(PaperDialogType.confirmation(yesButton.paperButton, noButton.paperButton))
	
	data class ServerLinks(val exitButton: DialogButton, val columns: Int = 2, val buttonWidth: Int = 150):
		DialogType(PaperDialogType.serverLinks(exitButton.paperButton, columns, buttonWidth))
	
	data class DialogList(
		val dialogs: List<Dialog>,
		val exitButton: DialogButton,
		val columns: Int = 2,
		val buttonWidth: Int = 150,
	): DialogType(
		PaperDialogType.dialogList(
			RegistrySet.valueSet(RegistryKey.DIALOG, dialogs),
			exitButton.paperButton,
			columns,
			buttonWidth
		)
	)
	
	data class MultiAction(
		val buttons: List<DialogButton>,
		val exitButton: DialogButton,
		val columns: Int = 2,
	): DialogType(PaperDialogType.multiAction(buttons.map { it.paperButton }, exitButton.paperButton, columns))
}
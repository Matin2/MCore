package me.matin.mcore.managers.dialog

import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.action.DialogAction
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.dialog.DialogLike
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import java.net.URL

@Suppress("UnstableApiUsage", "unused")
data class DialogButton(
	val label: Component,
	val action: Action,
	val tooltip: Component? = null,
	val width: Int = 150,
) {
	
	typealias CustomAction = DialogResponseView.(Audience) -> Unit
	
	private var customAction: (CustomAction)? = null
	internal val paperButton: ActionButton = ActionButton.create(label, tooltip, width, customAction?.let { action ->
		DialogAction.customClick(action, ClickCallback.Options.builder().build())
	} ?: action.paperAction)
	
	constructor(
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
		action: CustomAction,
	): this(label, Action.None, tooltip, width) {
		customAction = action
	}
	
	@Suppress("unused")
	sealed class Action(val paperAction: DialogAction?) {
		
		data class OpenURL(val url: URL): Action(DialogAction.staticAction(ClickEvent.openUrl(url)))
		data class RunCommand(val command: String): Action(DialogAction.staticAction(ClickEvent.runCommand(command)))
		data class RunCommandTemplate(val template: String): Action(DialogAction.commandTemplate(template))
		data class SuggestCommand(val command: String):
			Action(DialogAction.staticAction(ClickEvent.suggestCommand(command)))
		
		data class ChangePage(val page: Int): Action(DialogAction.staticAction(ClickEvent.changePage(page)))
		data class CopyToClipboard(val value: String):
			Action(DialogAction.staticAction(ClickEvent.copyToClipboard(value)))
		
		data class ShowDialog(val dialog: DialogLike): Action(DialogAction.staticAction(ClickEvent.showDialog(dialog)))
		data class Custom(val id: Key): Action(DialogAction.customClick(id, null))
		data object None: Action(null)
	}
}
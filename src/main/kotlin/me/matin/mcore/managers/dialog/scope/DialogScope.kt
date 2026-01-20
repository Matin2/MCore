package me.matin.mcore.managers.dialog.scope

import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import me.matin.mcore.managers.FloatProgression
import me.matin.mcore.managers.dialog.DialogInputValue
import me.matin.mcore.managers.dialog.DialogOption
import me.matin.mcore.managers.dialog.EnumDialogOption
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.dialog.DialogLike
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.inventory.ItemStack
import java.net.URL
import kotlin.enums.enumEntries
import kotlin.reflect.KClass

@Suppress("unused", "UnstableApiUsage", "NOTHING_TO_INLINE")
sealed class DialogScope(internal var titleProperty: Component) {
	
	val title get() = titleProperty
	var externalTitle: Component = title
	var escapeCloses: Boolean = true
	var afterAction: DialogBase.DialogAfterAction = DialogBase.DialogAfterAction.CLOSE
	
	val body: List<DialogBody>
		field : MutableList<DialogBody> = mutableListOf()
	val inputs: List<DialogInput>
		field : MutableList<DialogInput> = mutableListOf()
	
	internal abstract val type: DialogType
	internal val base: DialogBase
		get() = DialogBase.create(
			title,
			externalTitle,
			escapeCloses,
			false,
			afterAction,
			body,
			inputs
		)
	
	fun messageBody(message: Component, width: Int = 200): PlainMessageDialogBody =
		DialogBody.plainMessage(message, width).also { body += it }
	
	
	fun itemBody(
		item: ItemStack,
		description: PlainMessageDialogBody,
		showDecorations: Boolean = true,
		showTooltip: Boolean = true,
		width: Int = 16,
		height: Int = 16,
	) {
		body += DialogBody.item(item, description, showDecorations, showTooltip, width, height)
	}
	
	fun textInput(
		key: String,
		label: Component,
		initial: String = "",
		maxLength: Int = 32,
		maxLines: Int? = null,
		width: Int = 200,
		height: Int? = null,
		labelVisible: Boolean = true,
	): DialogInputValue<String> {
		inputs += DialogInput.text(
			key, width, label, labelVisible, initial, maxLength, if (maxLines != null && height != null)
				TextDialogInput.MultilineOptions.create(maxLines, height)
			else null
		)
		return DialogInputValue(key, String::class)
	}
	
	fun booleanInput(key: String, label: Component, initial: Boolean): DialogInputValue<Boolean> {
		inputs += DialogInput.bool(key, label, initial, "true", "false")
		return DialogInputValue(key, Boolean::class)
	}
	
	fun optionInput(
		key: String,
		label: Component,
		initial: DialogOption<String>,
		vararg options: DialogOption<String>,
		width: Int = 200,
		labelVisible: Boolean = true,
	): DialogInputValue<String> {
		inputs += DialogInput.singleOption(key, width, buildList {
			add(SingleOptionDialogInput.OptionEntry.create(initial.id, initial.display, true))
			options.mapTo(this) { (id, display) ->
				SingleOptionDialogInput.OptionEntry.create(id, display, false)
			}
		}, label, labelVisible)
		return DialogInputValue(key, String::class)
	}
	
	fun <T : Enum<T>> optionInput(
		key: String,
		label: Component,
		clazz: KClass<T>,
		initial: DialogOption<T>,
		vararg options: DialogOption<T>,
		width: Int = 200,
		labelVisible: Boolean = true,
	): DialogInputValue<T> {
		inputs += DialogInput.singleOption(key, width, buildList {
			add(SingleOptionDialogInput.OptionEntry.create(initial.id.name, initial.display, true))
			options.mapTo(this) { (id, display) ->
				SingleOptionDialogInput.OptionEntry.create(id.name, display, false)
			}
		}, label, labelVisible)
		return DialogInputValue(key, clazz)
	}
	
	inline fun <reified T : Enum<T>> optionInput(
		key: String,
		label: Component,
		initial: DialogOption<T>,
		vararg options: DialogOption<T>,
		width: Int = 200,
		labelVisible: Boolean = true,
	): DialogInputValue<T> =
		optionInput(key, label, T::class, initial, options = options, width, labelVisible)
	
	inline fun <reified T> optionInput(
		key: String,
		label: Component,
		initial: T,
		width: Int = 200,
		labelVisible: Boolean = true,
	): DialogInputValue<T> where T : Enum<T>, T : EnumDialogOption = optionInput(
		key, label, DialogOption(initial, initial.display),
		options = enumEntries<T>().map<T, DialogOption<T>> { DialogOption(it, it.display) }.toTypedArray(),
		width, labelVisible
	)
	
	fun rangeInput(
		key: String,
		label: Component,
		range: ClosedFloatingPointRange<Float>,
		initial: Float,
		width: Int = 200,
		labelFormat: String = "options.generic_value",
	): DialogInputValue<Float> {
		inputs += DialogInput.numberRange(
			key, width, label, labelFormat, range.start, range.endInclusive, initial,
			(range as? FloatProgression<Float>)?.step
		)
		return DialogInputValue(key, Float::class)
	}
	
	inline fun button(
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
		noinline action: DialogResponseView.(Audience) -> Unit,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		DialogAction.customClick(action, ClickCallback.Options.builder().build())
	)
	
	inline fun urlButton(
		label: Component,
		url: URL,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		DialogAction.staticAction(ClickEvent.openUrl(url))
	)
	
	inline fun urlButton(
		label: Component,
		url: String,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		DialogAction.staticAction(ClickEvent.openUrl(url))
	)
	
	inline fun commandButton(
		label: Component,
		command: String,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		if (command.contains("\\$\\((?<infix>[a-zA-Z_\\-0-9]+)\\)".toRegex())) DialogAction.commandTemplate(command)
		else DialogAction.staticAction(ClickEvent.runCommand(command))
	)
	
	inline fun suggestionButton(
		label: Component,
		suggestion: String,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		DialogAction.staticAction(ClickEvent.suggestCommand(suggestion))
	)
	
	inline fun changePageButton(
		label: Component,
		page: Int,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		DialogAction.staticAction(ClickEvent.changePage(page))
	)
	
	inline fun copyButton(
		label: Component,
		value: String,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		DialogAction.staticAction(ClickEvent.copyToClipboard(value))
	)
	
	inline fun showDialogButton(
		label: Component,
		dialog: DialogLike,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		DialogAction.staticAction(ClickEvent.showDialog(dialog))
	)
}
package com.github.matin2.mcore.services.dialog.context

import com.github.matin2.mcore.services.FloatProgression
import com.github.matin2.mcore.services.dialog.DialogButtonBlock
import com.github.matin2.mcore.services.dialog.DialogInputHolder
import com.github.matin2.mcore.services.dialog.DialogOption
import com.github.matin2.mcore.services.dialog.DialogService
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.dialog.DialogLike
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.inventory.ItemStack
import java.net.URL
import kotlin.enums.enumEntries

@DslMarker
annotation class DialogDsl

@DialogDsl
@Suppress("unused", "UnstableApiUsage", "NOTHING_TO_INLINE")
sealed class DialogContext(var title: Component) {
	
	var externalTitle: Component = title
	var escapeCloses: Boolean = true
	var afterAction: DialogBase.DialogAfterAction = CLOSE
	
	val body: MutableList<DialogBody> = []
	val inputs: MutableList<DialogInput> = []
	
	protected abstract val type: DialogType
	private inline val base: DialogBase
		get() = DialogBase.create(title, externalTitle, escapeCloses, false, afterAction, body, inputs)
	
	internal fun applyTo(builder: DialogRegistryEntry.Builder) =
		builder.base(base).type(type)
	
	fun messageBody(message: Component, width: Int = 200) {
		body += DialogBody.plainMessage(message, width)
	}
	
	
	fun itemBody(
		item: ItemStack,
		description: Component,
		showDecorations: Boolean = true,
		showTooltip: Boolean = true,
		width: Int = 16,
		height: Int = 16,
		descriptionWidth: Int = 200,
	) {
		val description = DialogBody.plainMessage(description, descriptionWidth)
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
	): DialogInputHolder<String> {
		val multilineOptions =
			if (maxLines != null && height != null) TextDialogInput.MultilineOptions.create(maxLines, height) else null
		inputs += DialogInput.text(key, width, label, labelVisible, initial, maxLength, multilineOptions)
		return object : DialogInputHolder<String>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = requireNotNull(ctx.view.getText(key))
		}
	}
	
	fun booleanInput(key: String, label: Component, initial: Boolean): DialogInputHolder<Boolean> {
		inputs += DialogInput.bool(key, label, initial, "true", "false")
		return object : DialogInputHolder<Boolean>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = requireNotNull(ctx.view.getBoolean(key))
		}
	}
	
	fun optionInput(
		key: String,
		label: Component,
		initial: DialogOption,
		vararg options: DialogOption,
		width: Int = 200,
		labelVisible: Boolean = true,
	): DialogInputHolder<String> {
		val optionEntries = buildList {
			add(SingleOptionDialogInput.OptionEntry.create(initial.id, initial.display, true))
			options.mapTo(this) {
				SingleOptionDialogInput.OptionEntry.create(it.id, it.display, false)
			}
		}
		inputs += DialogInput.singleOption(key, width, optionEntries, label, labelVisible)
		return object : DialogInputHolder<String>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = requireNotNull(ctx.view.getText(key))
		}
	}
	
	inline fun <reified E : Enum<E>> optionInput(
		key: String,
		label: Component,
		initial: E,
		width: Int = 200,
		labelVisible: Boolean = true,
		labeler: (option: E) -> Component? = { null },
	): DialogInputHolder<E> {
		val optionEntries = enumEntries<E>().map {
			SingleOptionDialogInput.OptionEntry.create(it.name, labeler(it), it == initial)
		}
		inputs += DialogInput.singleOption(key, width, optionEntries, label, labelVisible)
		return object : DialogInputHolder<E>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = enumValueOf<E>(requireNotNull(ctx.view.getText(key)))
		}
	}
	
	fun rangeInput(
		key: String, label: Component,
		start: Float, end: Float, initial: Float, step: Float? = null,
		width: Int = 200, labelFormat: String = "options.generic_value",
	): DialogInputHolder<Float> {
		inputs += DialogInput.numberRange(key, width, label, labelFormat, start, end, initial, step)
		return object : DialogInputHolder<Float>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = requireNotNull(ctx.view.getFloat(key))
		}
	}
	
	inline fun rangeInput(
		key: String,
		label: Component,
		range: ClosedFloatingPointRange<Float>,
		initial: Float,
		step: Float? = null,
		width: Int = 200,
		labelFormat: String = "options.generic_value",
	) = rangeInput(key, label, range.start, range.endInclusive, initial, step, width, labelFormat)
	
	inline fun rangeInput(
		key: String,
		label: Component,
		range: FloatProgression,
		initial: Float,
		width: Int = 200,
		labelFormat: String = "options.generic_value",
	) = rangeInput(key, label, range, initial, range.step, width, labelFormat)
	
	fun button(
		key: Key,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
		action: DialogButtonBlock
	): ActionButton {
		DialogService.buttonActions[key] = action
		return ActionButton.create(label, tooltip, width, DialogAction.customClick(key, null))
	}
	
	inline fun urlButton(
		url: URL,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width, DialogAction.staticAction(ClickEvent.openUrl(url))
	)
	
	inline fun urlButton(
		url: String,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width, DialogAction.staticAction(ClickEvent.openUrl(url))
	)
	
	inline fun commandButton(
		command: String,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width,
		if ("\\$\\((?<infix>[a-zA-Z_\\-0-9]+)\\)".toRegex() in command) DialogAction.commandTemplate(command)
		else DialogAction.staticAction(ClickEvent.runCommand(command))
	)
	
	inline fun suggestionButton(
		suggestion: String,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width, DialogAction.staticAction(ClickEvent.suggestCommand(suggestion))
	)
	
	inline fun changePageButton(
		page: Int,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width, DialogAction.staticAction(ClickEvent.changePage(page))
	)
	
	inline fun copyButton(
		value: String,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width, DialogAction.staticAction(ClickEvent.copyToClipboard(value))
	)
	
	inline fun showDialogButton(
		dialog: DialogLike,
		label: Component,
		tooltip: Component? = null,
		width: Int = 150,
	): ActionButton = ActionButton.create(
		label, tooltip, width, DialogAction.staticAction(ClickEvent.showDialog(dialog))
	)
}

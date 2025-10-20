package me.matin.mcore.managers.dialog.element

import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import net.kyori.adventure.text.Component
import io.papermc.paper.registry.data.dialog.input.DialogInput as PaperInput

@Suppress("UnstableApiUsage", "unused")
sealed class DialogInput(val paperInput: PaperInput): DialogElement {
	
	data class Text(
		val key: String,
		val label: Component,
		val width: Int = 200,
		val labelVisible: Boolean = true,
		val initial: String = "",
		val maxLength: Int = 32,
		val maxLines: Int? = null,
		val height: Int? = null,
	): DialogInput(
		PaperInput.text(
			key, width, label, labelVisible, initial, maxLength,
			if (maxLines != null && height != null)
				TextDialogInput.MultilineOptions.create(maxLines, height)
			else null
		)
	) {
		
		companion object {
			
			context(response: DialogResponseView)
			operator fun get(key: String) = response.getText(key) ?: error("Unrecognized key: $key")
		}
	}
	
	data class Bool(val key: String, val label: Component, val initial: Boolean):
		DialogInput(PaperInput.bool(key, label, initial, "true", "false")) {
		
		companion object {
			
			context(response: DialogResponseView)
			operator fun get(key: String) = response.getBoolean(key) ?: error("Unrecognized key: $key")
		}
	}
	
	data class SingleOption(
		val key: String,
		val label: Component,
		val options: Map<String, Component?>,
		val initialID: String? = null,
		val width: Int = 200,
		val labelVisible: Boolean = true,
	): DialogInput(
		PaperInput.singleOption(key, width, options.toList().mapIndexed { index, (id, display) ->
			SingleOptionDialogInput.OptionEntry.create(
				id, display, when (initialID) {
					id -> true
					null if index == 0 -> true
					else -> false
				}
			)
		}, label, labelVisible)
	) {
		
		companion object {
			
			context(response: DialogResponseView)
			operator fun get(key: String) = response.getText(key) ?: error("Unrecognized key: $key")
		}
	}
	
	data class SingleEnumOption<T: Enum<T>>(
		val key: String,
		val label: Component,
		val options: Map<T, Component?>,
		val initial: T? = null,
		val width: Int = 200,
		val labelVisible: Boolean = true,
	): DialogInput(
		PaperInput.singleOption(key, width, options.toList().mapIndexed { index, (id, display) ->
			SingleOptionDialogInput.OptionEntry.create(
				id.name, display, when (initial) {
					id -> true
					null if index == 0 -> true
					else -> false
				}
			)
		}, label, labelVisible)
	) {
		
		companion object {
			
			context(response: DialogResponseView)
			inline operator fun <reified T: Enum<T>> get(key: String) =
				enumValueOf<T>(response.getText(key) ?: error("Unrecognized key: $key"))
		}
	}
	
	data class NumberRange(
		val key: String,
		val label: Component,
		val start: Float,
		val end: Float,
		val initial: Float,
		val step: Float? = null,
		val width: Int = 200,
		val labelFormat: String = "options.generic_value",
	): DialogInput(PaperInput.numberRange(key, width, label, labelFormat, start, end, initial, step)) {
		
		companion object {
			
			context(response: DialogResponseView)
			operator fun get(key: String) = response.getFloat(key) ?: error("Unrecognized key: $key")
		}
	}
}

@file:Suppress("UnstableApiUsage", "unused")

package me.matin.mcore.managers.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import me.matin.mcore.managers.dialog.element.DialogBody
import me.matin.mcore.managers.dialog.element.DialogElement
import me.matin.mcore.managers.dialog.element.DialogInput
import net.kyori.adventure.text.Component

data class DialogTemplate(
	var type: DialogType,
	var title: Component,
	var body: List<DialogBody> = emptyList(),
	var inputs: List<DialogInput> = emptyList(),
	var externalTitle: Component? = null,
	var escapeCloses: Boolean = true,
	var afterAction: DialogAfterAction = DialogAfterAction.CLOSE,
) {
	
	constructor(
		type: DialogType,
		title: Component,
		externalTitle: Component? = null,
		escapeCloses: Boolean = true,
		afterAction: DialogAfterAction = DialogAfterAction.CLOSE,
		vararg elements: DialogElement,
	): this(
		type,
		title,
		elements.filterIsInstance<DialogBody>(),
		elements.filterIsInstance<DialogInput>(),
		externalTitle,
		escapeCloses,
		afterAction
	)
	
	infix fun applyTo(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder {
		val paperBase = DialogBase.create(
			title,
			externalTitle,
			escapeCloses,
			false,
			afterAction.paperAfterAction,
			body.map { it.value },
			inputs.map { it.paperInput }
		)
		return builder.type(type.paperType).base(paperBase)
	}
	
	fun buildDialog(): Dialog = Dialog.create { applyTo(it.empty()) }
}

enum class DialogAfterAction(val paperAfterAction: DialogBase.DialogAfterAction) {
	CLOSE(DialogBase.DialogAfterAction.CLOSE),
	NONE(DialogBase.DialogAfterAction.NONE),
	WAIT_FOR_RESPONSE(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE);
}
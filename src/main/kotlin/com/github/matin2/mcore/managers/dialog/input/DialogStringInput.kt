package com.github.matin2.mcore.managers.dialog.input

import io.papermc.paper.dialog.DialogResponseView

@Suppress("UnstableApiUsage")
internal class DialogStringInput(override val key: String) : DialogTypedInput<String> {
	
	context(view: DialogResponseView)
	override val value get() = requireNotNull(view.getText(key))
	
	override fun toString() = "$($key)"
}

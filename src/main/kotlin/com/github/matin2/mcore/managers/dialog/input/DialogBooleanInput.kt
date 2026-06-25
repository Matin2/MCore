package com.github.matin2.mcore.managers.dialog.input

import io.papermc.paper.dialog.DialogResponseView

@Suppress("UnstableApiUsage")
internal class DialogBooleanInput(override val key: String) : DialogTypedInput<Boolean> {
	
	context(view: DialogResponseView)
	override val value get() = requireNotNull(view.getBoolean(key))
	
	override fun toString() = "$($key)"
}

package com.github.matin2.mcore.managers.dialog.input

import io.papermc.paper.dialog.DialogResponseView

@Suppress("UnstableApiUsage")
internal class DialogFloatInput(override val key: String) : DialogTypedInput<Float> {
	
	context(view: DialogResponseView)
	override val value get() = requireNotNull(view.getFloat(key))
	
	override fun toString() = "$($key)"
}

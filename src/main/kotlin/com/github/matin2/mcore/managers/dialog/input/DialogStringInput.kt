package com.github.matin2.mcore.managers.dialog.input

import com.github.matin2.mcore.managers.dialog.context.DialogInputsContext

@Suppress("UnstableApiUsage")
internal class DialogStringInput(override val key: String) : DialogTypedInput<String> {
	
	context(ctx: DialogInputsContext)
	override val value get() = requireNotNull(ctx.view.getText(key))
	
	override fun toString() = "$($key)"
}

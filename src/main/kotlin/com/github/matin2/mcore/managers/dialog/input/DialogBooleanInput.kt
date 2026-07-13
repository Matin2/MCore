package com.github.matin2.mcore.managers.dialog.input

import com.github.matin2.mcore.managers.dialog.context.DialogInputsContext

@Suppress("UnstableApiUsage")
internal class DialogBooleanInput(override val key: String) : DialogTypedInput<Boolean> {
	
	context(ctx: DialogInputsContext)
	override val value get() = requireNotNull(ctx.view.getBoolean(key))
	
	override fun toString() = "$($key)"
}

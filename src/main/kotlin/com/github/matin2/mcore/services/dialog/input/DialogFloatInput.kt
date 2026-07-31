package com.github.matin2.mcore.services.dialog.input

import com.github.matin2.mcore.services.dialog.context.DialogInputsContext

@Suppress("UnstableApiUsage")
internal class DialogFloatInput(override val key: String) : DialogTypedInput<Float> {
	
	context(ctx: DialogInputsContext)
	override val value get() = requireNotNull(ctx.view.getFloat(key))
	
	override fun toString() = "$($key)"
}

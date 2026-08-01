package com.github.matin2.mcore.services.dialog

import com.github.matin2.mcore.services.dialog.context.DialogInputsContext

abstract class DialogTypedInput<T>(val key: String) {
	
	context(_: DialogInputsContext)
	abstract val value: T
	
	operator fun component1() = key
	
	context(_: DialogInputsContext)
	operator fun component2() = value
	
	override fun toString() = "$($key)"
}

package com.github.matin2.mcore.managers.dialog.input

import com.github.matin2.mcore.managers.dialog.context.DialogInputsContext

interface DialogTypedInput<T> {
	val key: String
	
	context(_: DialogInputsContext)
	val value: T
	
	operator fun component1() = key
	
	context(_: DialogInputsContext)
	operator fun component2() = value
}

package com.github.matin2.mcore.managers.dialog.input

import io.papermc.paper.dialog.DialogResponseView

@Suppress("UnstableApiUsage")
interface DialogTypedInput<T> {
	val key: String
	
	context(view: DialogResponseView)
	val value: T
	
	operator fun component1() = key
	
	context(view: DialogResponseView)
	operator fun component2() = value
}

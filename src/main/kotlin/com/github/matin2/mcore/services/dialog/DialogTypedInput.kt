package com.github.matin2.mcore.services.dialog

import com.github.matin2.mcore.services.dialog.context.DialogInputsContext

@Suppress("UnstableApiUsage", "NOTHING_TO_INLINE")
abstract class DialogTypedInput<T>(val key: String) {
	
	context(_: DialogInputsContext)
	abstract val value: T
	
	operator fun component1() = key
	
	context(_: DialogInputsContext)
	operator fun component2() = value
	
	override fun toString() = "$($key)"
	
	companion {
		internal inline fun string(key: String) = object : DialogTypedInput<String>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = requireNotNull(ctx.view.getText(key))
		}
		
		internal inline fun boolean(key: String) = object : DialogTypedInput<Boolean>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = requireNotNull(ctx.view.getBoolean(key))
		}
		
		internal inline fun float(key: String) = object : DialogTypedInput<Float>(key) {
			context(ctx: DialogInputsContext)
			override val value get() = requireNotNull(ctx.view.getFloat(key))
		}
	}
}

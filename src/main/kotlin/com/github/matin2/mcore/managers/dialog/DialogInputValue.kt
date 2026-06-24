package com.github.matin2.mcore.managers.dialog

import io.papermc.paper.dialog.DialogResponseView
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

@Suppress("UnstableApiUsage")
class DialogInputValue<T : Any> internal constructor(val key: String, private val clazz: KClass<T>) {
	
	private lateinit var _value: T
	
	context(view: DialogResponseView)
	val value: T
		get() {
			if (!::_value.isInitialized) _value = when (clazz) {
				String::class -> clazz.cast(view.getText(key))
				Boolean::class -> clazz.cast(view.getBoolean(key))
				Float::class -> clazz.cast(view.getFloat(key))
				else -> clazz.java.enumConstants?.first {
					it.toString().equals(view.getText(key), true)
				} ?: error("Invalid class type: ${clazz.simpleName}")
			}
			return _value
		}
	
	operator fun component1() = key
	
	context(view: DialogResponseView)
	operator fun component2() = value
	
	override fun toString() = "$($key)"
}

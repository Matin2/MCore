package com.github.matin2.mcore.managers.dialog.context

import com.github.matin2.mcore.managers.dialog.input.DialogTypedInput
import io.papermc.paper.dialog.DialogResponseView
import kotlin.reflect.KProperty

@Suppress("UnstableApiUsage")
@JvmInline
value class DialogInputsContext(internal val view: DialogResponseView) {
	
	operator fun <T> DialogTypedInput<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
}

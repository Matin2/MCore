package com.github.matin2.mcore.services.dialog.context

import com.github.matin2.mcore.services.dialog.DialogInputHolder
import io.papermc.paper.dialog.DialogResponseView
import kotlin.reflect.KProperty

@Suppress("UnstableApiUsage")
@JvmInline
value class DialogInputsContext(val view: DialogResponseView) {
	
	operator fun <T> DialogInputHolder<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
}

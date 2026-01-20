@file:Suppress("NOTHING_TO_INLINE", "unused")

package me.matin.mcore.managers.dialog

import me.matin.mcore.managers.dialog.scope.DialogScope
import net.kyori.adventure.text.Component

data class DialogOption<T : Any>(val id: T, val display: Component?) {
	
	companion object {
		
		@JvmStatic
		context(_: DialogScope)
		inline val String.option get() = DialogOption(this, null)
		
		@JvmStatic
		context(_: DialogScope)
		inline val <T : Enum<T>> T.option get() = DialogOption(this, null)
		
		@JvmStatic
		context(_: DialogScope)
		inline infix fun String.labeled(label: Component) = DialogOption(this, label)
		
		@JvmStatic
		context(_: DialogScope)
		inline infix fun <T : Enum<T>> T.labeled(label: Component) = DialogOption(this, label)
	}
}

interface EnumDialogOption {
	
	val display: Component?
}
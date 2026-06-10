@file:Suppress("NOTHING_TO_INLINE", "unused")

package me.matin.mcore.managers.dialog

import me.matin.mcore.managers.dialog.scope.DialogScope
import net.kyori.adventure.text.Component

data class DialogOption(val id: String, val display: Component?)

context(_: DialogScope)
inline val String.option get() = DialogOption(this, null)

context(_: DialogScope)
inline infix fun String.labeled(label: Component) = DialogOption(this, label)

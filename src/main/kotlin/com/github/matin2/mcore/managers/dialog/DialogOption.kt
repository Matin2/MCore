@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.matin2.mcore.managers.dialog

import com.github.matin2.mcore.managers.dialog.scope.DialogContext
import net.kyori.adventure.text.Component

data class DialogOption(val id: String, val display: Component?)

context(_: DialogContext)
inline val String.option get() = DialogOption(this, null)

context(_: DialogContext)
inline infix fun String.labeled(label: Component) = DialogOption(this, label)

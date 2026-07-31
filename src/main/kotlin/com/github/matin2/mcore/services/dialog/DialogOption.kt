@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.matin2.mcore.services.dialog

import com.github.matin2.mcore.services.dialog.context.DialogContext
import net.kyori.adventure.text.Component

data class DialogOption(val id: String, val display: Component?)

context(_: DialogContext)
inline fun String.option() = DialogOption(this, null)

context(_: DialogContext)
inline infix fun String.option(label: Component) = DialogOption(this, label)

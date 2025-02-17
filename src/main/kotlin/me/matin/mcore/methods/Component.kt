@file:Suppress("unused")

package me.matin.mcore.methods

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Returns a component representation of the object.
 *
 * @see toString
 */
fun String.toComponent() = Component.text(this)

/**
 * Returns a component representation of the object.
 *
 * @param style Style of the component.
 * @see toString
 */
infix fun String.toComponent(style: Style) = Component.text(this, style)

/**
 * Returns a component representation of the object.
 *
 * @param color Color of the component.
 * @see toString
 */
infix fun String.toComponent(color: TextColor?) = Component.text(this, color)

/**
 * Returns a component representation of the object.
 *
 * @param color Color of the component.
 * @param decorations Decorations of the component.
 * @see toString
 */
fun String.toComponent(
	color: TextColor?,
	vararg decorations: TextDecoration,
) = Component.text(this, color, *decorations)

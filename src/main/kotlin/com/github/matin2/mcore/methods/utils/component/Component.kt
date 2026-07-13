@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.matin2.mcore.methods.utils.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Creates an empty component.
 *
 * @see Component.empty
 */
inline fun component() = Component.empty()

/**
 * Creates a newline text component.
 *
 * @see Component.newline
 */
inline fun newline() = Component.newline()

/**
 * Creates a space text component.
 *
 * @see Component.space
 */
inline fun space() = Component.space()

/**
 * Creates a text component with the specified [text] and [style].
 *
 * @param text plain text content
 * @param style styling to use
 * @see Component.text
 */
inline fun component(text: String, style: Style) = Component.text(text, style)

/**
 * Creates a text component with the specified [text], and optional [color]
 * and [decorations].
 *
 * @param text plain text content
 * @param color text color
 * @param decorations text decorations
 * @see Component.text
 */
inline fun component(
	text: String,
	color: TextColor? = null,
	vararg decorations: TextDecoration
) = Component.text(text, color, *decorations)

/**
 * Creates a text component builder and applies the given [builder] to it.
 *
 * @param builder block to apply to the builder
 * @return built text component
 * @see Component.text
 * @see ComponentBuilder
 */
inline fun component(builder: TextComponent.Builder.() -> Unit) = Component.text().apply(builder).build()

/**
 * Creates a text component by appending [other] to [this] component.
 *
 * @param other the component to append
 * @receiver the original component
 * @see append
 */
inline operator fun Component.plus(other: ComponentLike) = append(other)

/**
 * Creates a text component by appending [other] to [this] component.
 *
 * @param other the component to append
 * @receiver the original component
 * @see append
 */
inline operator fun Component.plus(other: ComponentBuilder<*, *>) = append(other)

/**
 * Creates a text component by appending each component from [other] to
 * [this] component.
 *
 * @param other the components to append
 * @receiver the original component
 * @see append
 */
inline operator fun Component.plus(other: List<ComponentLike>) = append(other)

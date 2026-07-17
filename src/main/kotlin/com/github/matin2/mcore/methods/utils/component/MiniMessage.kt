@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.matin2.mcore.methods.utils.component

import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

inline fun minimessage(preset: MiniMessage.Preset = DEFAULT) = MiniMessage.miniMessage(preset)

inline fun minimessage(
	preset: MiniMessage.Preset = DEFAULT,
	builder: MiniMessage.Builder.() -> Unit = {}
) = MiniMessage.builder(preset).apply(builder).build()

inline fun MiniMessage.Builder.tags(
	crossinline builder: TagResolver.Builder.() -> Unit = {}
) = editTags { builder(it) }

//Deserialization

inline operator fun MiniMessage.invoke(serialized: String): Component = deserialize(serialized)

inline operator fun MiniMessage.invoke(serialized: String, target: Pointered) =
	deserialize(serialized, target)

inline operator fun MiniMessage.invoke(serialized: String, resolver: TagResolver) =
	deserialize(serialized, resolver)

inline operator fun MiniMessage.invoke(serialized: String, target: Pointered, resolver: TagResolver) =
	deserialize(serialized, target, resolver)

inline operator fun MiniMessage.invoke(serialized: String, vararg resolvers: TagResolver) =
	deserialize(serialized, *resolvers)

inline operator fun MiniMessage.invoke(
	serialized: String,
	target: Pointered,
	vararg resolvers: TagResolver
): Component = deserialize(serialized, target, *resolvers)

//Serialization

inline operator fun MiniMessage.invoke(component: Component): String = serialize(component)
inline operator fun MiniMessage.invoke(component: Component, fallback: String) = serializeOr(component, fallback)!!

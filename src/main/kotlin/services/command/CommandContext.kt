package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus.Internal
import kotlin.reflect.KProperty
import com.mojang.brigadier.context.CommandContext as BackedContext

@Suppress("unused", "NOTHING_TO_INLINE")
@CommandDsl
open class CommandContext internal constructor(@Internal val context: BackedContext<CommandSourceStack>) {
	
	inline val source: CommandSourceStack get() = context.source
	inline val input: String get() = context.input
	
	inline operator fun <reified T : Any> ArgumentHolder<T>.invoke(): T = context.getArgument(name, T::class.java)
	inline operator fun <reified T : Any> ArgumentHolder<T>.getValue(thisRef: T?, property: KProperty<*>) = invoke()
	
	inline operator fun <reified T : Any> ArgumentHolder<T?>.invoke() = try {
		context.getArgument(name, T::class.java)
	} catch (_: IllegalArgumentException) {
		null
	}
	
	inline operator fun <reified T : Any> ArgumentHolder<T?>.invoke(default: T): T = try {
		context.getArgument(name, T::class.java)
	} catch (_: IllegalArgumentException) {
		default
	}
	
	inline fun <reified T : Any> ArgumentHolder<T?>.orElse(block: () -> T): T = try {
		context.getArgument(name, T::class.java)
	} catch (_: IllegalArgumentException) {
		block()
	}
	
	inline operator fun <reified T : Any> ArgumentHolder<T?>.getValue(thisRef: T?, property: KProperty<*>) = invoke()
	
	inline fun fail(message: Component): Nothing =
		throw SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(message)).create()
	
	inline fun fail(message: String): Nothing = throw SimpleCommandExceptionType(LiteralMessage(message)).create()
}

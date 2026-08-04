package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KProperty

@Suppress("unused", "NOTHING_TO_INLINE")
@CommandDsl
@JvmInline
value class CommandExecutionContext internal constructor(@ApiStatus.Internal val context: CommandContext<CommandSourceStack>) {
	
	inline val source: CommandSourceStack get() = context.source
	inline val input: String get() = context.input
	
	inline operator fun <reified T : Any> ArgumentHolder<T>.invoke() = context.getArgument(name, T::class.java)!!
	inline operator fun <reified T : Any> ArgumentHolder<T>.getValue(thisRef: T?, property: KProperty<*>) = invoke()
	
	inline fun fail(message: Component): Nothing =
		throw SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(message)).create()
	
	inline fun fail(message: String): Nothing = throw SimpleCommandExceptionType(LiteralMessage(message)).create()
}

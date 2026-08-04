package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.CommandSource
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus

@Suppress("unused", "NOTHING_TO_INLINE")
@CommandDsl
@JvmInline
value class CommandExecutionContext internal constructor(@ApiStatus.Internal val context: CommandContext<CommandSourceStack>) {
	
	inline val source: CommandSource get() = CommandSource(context.source)
	inline val input: String get() = context.input
	
	inline fun fail(message: Component): Nothing =
		throw SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(message)).create()
	
	inline fun fail(message: String): Nothing = fail(component(message))
}

package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.utils.component.component
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.ParsedCommandNode
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus

@Suppress("unused", "NOTHING_TO_INLINE")
@CommandDsl
@JvmInline
value class CommandExecutionContext(@ApiStatus.Internal val context: CommandContext<CommandSourceStack>) {
	
	inline val source: CommandSourceStack get() = context.source
	inline val nodes: List<ParsedCommandNode<CommandSourceStack>> get() = context.nodes
	inline val input: String get() = context.input
	inline val range: StringRange get() = context.range
	
	inline fun fail(message: Component): Nothing =
		throw SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(message)).create()
	
	inline fun fail(message: String): Nothing = fail(component(message))
}

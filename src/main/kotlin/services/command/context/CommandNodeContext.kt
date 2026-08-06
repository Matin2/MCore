package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.CommandRequirement
import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.github.matin2.mcore.services.command.execution.CommandExecutor
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@CommandDsl
@Suppress("NOTHING_TO_INLINE")
sealed class CommandNodeContext<Builder : ArgumentBuilder<CommandSourceStack, Builder>> {
	
	private typealias LiteralBlock = CommandLiteral.() -> Unit
	private typealias ArgumentBlock<T> = CommandArgument<T>.(holder: ArgumentHolder<T>) -> Unit
	
	protected abstract val builder: Builder
	internal abstract val scope: () -> CoroutineScope?
	
	val requires = CommandRequirement()
	val executes = CommandExecutor()
	
	internal open fun finalize() = requires.addTo(builder).executes(executes.build(scope))
	
	fun literal(name: String, aliases: Collection<String>, block: LiteralBlock) {
		val context = CommandLiteral(name, aliases, scope)
		context.block()
		val node = context.finalize().build()
		builder.then(node)
		context.aliases.forEach { builder.then(Commands.literal(it).redirect(node)) }
	}
	
	inline fun literal(name: String, vararg aliases: String, noinline block: LiteralBlock) =
		literal(name, aliases.toList(), block)
	
	inline operator fun String.invoke(vararg aliases: String, noinline block: LiteralBlock) =
		literal(this, aliases = aliases, block)
	
	inline operator fun Collection<String>.invoke(noinline block: LiteralBlock) =
		literal(first(), drop(1), block)
	
	fun <T : Any> argument(name: String, type: ArgumentType<T>, block: ArgumentBlock<T>) {
		val context = CommandArgument(name, type, scope)
		context.block(ArgumentHolder(name))
		builder.then(context.finalize())
	}
}

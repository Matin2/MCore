package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.CommandRequirement
import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.github.matin2.mcore.services.command.execution.CommandExecution
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@CommandDsl
@Suppress("NOTHING_TO_INLINE")
sealed class CommandNodeContext<Builder : ArgumentBuilder<CommandSourceStack, Builder>> {
	
	private typealias LiteralBlock = LiteralContext.() -> Unit
	private typealias ArgumentBlock<T> = ArgumentContext<T>.(holder: ArgumentHolder<T>) -> Unit
	
	protected abstract val builder: Builder
	internal abstract val scope: () -> CoroutineScope?
	
	val requires = CommandRequirement()
	val executes = CommandExecution()
	
	internal open fun finalize() = requires.addTo(builder).executes(executes.build(scope))
	
	fun literal(name: String, aliases: List<String>, action: LiteralBlock) {
		val context = LiteralContext(name, aliases, scope)
		context.action()
		val node = context.finalize().build()
		builder.then(node)
		context.aliases.forEach { builder.then(Commands.literal(it).redirect(node)) }
	}
	
	inline fun literal(name: String, vararg aliases: String, noinline action: LiteralBlock) =
		literal(name, aliases.toList(), action)
	
	inline operator fun String.invoke(vararg aliases: String, noinline action: LiteralBlock) =
		literal(this, aliases = aliases, action)
	
	inline operator fun Collection<String>.invoke(noinline action: LiteralBlock) =
		literal(first(), drop(1), action)
	
	fun <T : Any> argument(name: String, type: ArgumentType<T>, action: ArgumentBlock<T>) {
		val context = ArgumentContext(name, type, scope)
		context.action(ArgumentHolder(name))
		builder.then(context.finalize())
	}
}

package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.argument.ArgumentContext
import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.github.matin2.mcore.services.command.execution.CommandExecution
import com.github.matin2.mcore.services.command.literal.LiteralContext
import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@CommandDsl
@Suppress("NOTHING_TO_INLINE")
abstract class CommandContext<Builder : ArgumentBuilder<CommandSourceStack, Builder>> {
	
	private typealias LiteralBlock = LiteralContext.() -> Unit
	private typealias ArgumentBlock<T> = ArgumentContext<T>.(holder: ArgumentHolder<T>) -> Unit
	
	internal abstract val scope: CoroutineScope?
	protected abstract val builder: Builder
	protected val scopeSetters: MutableList<(CoroutineScope?) -> Unit> = []
	
	val requires = CommandRequirement()
	val executes = CommandExecution()
	
	internal open fun finalizeBuilder() = builder.requires {
		executes.requires(it) && requires(it)
	}.executes(executes.build { scope })
	
	fun literal(name: String, aliases: List<String>, action: LiteralBlock) = command(name, aliases, action).run {
		scopeSetters += context::scope.setter
		builder.then(node)
		context.aliases.forEach { builder.then(Commands.literal(it).redirect(node)) }
	}
	
	inline fun literal(name: String, vararg aliases: String, noinline action: LiteralBlock) =
		literal(name, aliases.toList(), action)
	
	inline operator fun String.invoke(vararg aliases: String, noinline action: LiteralBlock) =
		literal(this, aliases = aliases, action)
	
	inline operator fun Collection<String>.invoke(noinline action: LiteralBlock) =
		literal(first(), drop(1), action)
}

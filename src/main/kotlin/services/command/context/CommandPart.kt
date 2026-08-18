package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.CommandExecutor
import com.github.matin2.mcore.services.command.CommandRequirement
import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.github.matin2.mcore.services.command.execution.CommandExecution
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

@CommandDsl
@Suppress("NOTHING_TO_INLINE", "unused")
sealed class CommandPart<Builder : ArgumentBuilder<CommandSourceStack, Builder>> {
	
	private typealias LiteralBlock = CommandLiteral.() -> Unit
	private typealias ArgumentBlock<T> = CommandArgument<T>.(argument: ArgumentHolder<T>) -> Unit
	private typealias ExecutionBlock = CommandExecution.() -> Unit
	
	protected abstract val builder: Builder
	internal abstract val scope: CommandCoroutineScope
	
	val requires = CommandRequirement()
	val executes = CommandExecutor()
	
	internal open fun finalize() = requires.addTo(builder).executes(executes.build(scope))
	
	fun literal(name: String, aliases: Collection<String>, block: LiteralBlock) {
		val context = object : CommandLiteral(name, aliases) {
			override val scope = this@CommandPart.scope
		}
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
	
	fun execution(block: ExecutionBlock) {
		val execution = CommandExecution(scope)
		execution.block()
		execution addTo builder
	}
}

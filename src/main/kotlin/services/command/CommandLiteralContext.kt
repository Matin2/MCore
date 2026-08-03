package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.execution.CommandLiteralExecution
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@CommandDsl
@Suppress("NOTHING_TO_INLINE", "unused")
class CommandLiteralContext internal constructor(name: String, vararg val aliases: String) {
	
	internal var scope: CoroutineScope? = null
		set(value) {
			field = value ?: return
			scopeSetters.forEach { it(value) }
		}
	private val builder = Commands.literal(name)
	private val requirements: MutableList<CommandSourcePredicate> = []
	private val scopeSetters: MutableList<(CoroutineScope?) -> Unit> = []
	val executes = CommandLiteralExecution()
	lateinit var description: String
	
	internal fun getDescription() = if (::description.isInitialized) description else null
	
	internal inline fun build() = CommandLiteral(this, builder.requires { source ->
		executes.executors.any { it.condition(source) } && requirements.all { it(source) }
	}.executes(executes.build { scope }).build())
	
	fun literal(name: String, vararg aliases: String, action: CommandLiteralContext.() -> Unit) =
		command(name, aliases = aliases, action).run {
			scopeSetters += context::scope.setter
			builder.then(mainNode)
			context.aliases.forEach { builder.then(Commands.literal(it).redirect(mainNode)) }
		}
	
	inline operator fun String.invoke(noinline action: CommandLiteralContext.() -> Unit) =
		literal(this, action = action)
	
	inline operator fun List<String>.invoke(noinline action: CommandLiteralContext.() -> Unit) =
		literal(first(), aliases = drop(1).toTypedArray(), action)
	
	fun requires(requirement: CommandSourcePredicate) {
		requirements += requirement
	}
}

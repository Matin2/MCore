package com.github.matin2.mcore.services.command.literal

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.CommandRequirement
import com.github.matin2.mcore.services.command.CommandSource
import com.github.matin2.mcore.services.command.command
import com.github.matin2.mcore.services.command.execution.CommandExecution
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@CommandDsl
@Suppress("NOTHING_TO_INLINE", "unused")
class CommandLiteralContext internal constructor(name: String, val aliases: Collection<String>) {
	
	internal var scope: CoroutineScope? = null
		set(value) {
			field = value ?: return
			scopeSetters.forEach { it(value) }
		}
	private val builder = Commands.literal(name)
	private val scopeSetters: MutableList<(CoroutineScope?) -> Unit> = []
	val requires = CommandRequirement()
	val executes = CommandExecution()
	lateinit var description: String
	
	internal fun getDescription() = if (::description.isInitialized) description else null
	
	internal inline fun build() = CommandLiteral(this, builder.requires {
		val source = CommandSource(it)
		executes.requires(source) && requires(source)
	}.executes(executes.build { scope }).build())
	
	fun literal(name: String, aliases: List<String>, action: CommandLiteralContext.() -> Unit) =
		command(name, aliases, action).run {
			scopeSetters += context::scope.setter
			builder.then(mainNode)
			context.aliases.forEach { builder.then(Commands.literal(it).redirect(mainNode)) }
		}
	
	inline fun literal(name: String, vararg aliases: String, noinline action: CommandLiteralContext.() -> Unit) =
		literal(name, aliases.toList(), action)
	
	inline operator fun String.invoke(vararg aliases: String, noinline action: CommandLiteralContext.() -> Unit) =
		literal(this, aliases = aliases, action)
	
	inline operator fun Collection<String>.invoke(noinline action: CommandLiteralContext.() -> Unit) =
		literal(first(), drop(1), action)
}

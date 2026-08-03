package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.execution.CommandExecution
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@CommandDsl
@Suppress("NOTHING_TO_INLINE", "unused")
class CommandLiteralContext(name: String, vararg val aliases: String) {
	
	private var scope: CoroutineScope? = null
	private val builder = Commands.literal(name)
	private val requirements: MutableList<CommandSourcePredicate> = []
	private val scopeSetters: MutableList<CommandScopeSetter> = []
	val executes = CommandExecution()
	lateinit var description: String
	
	fun requires(requirement: CommandSourcePredicate) {
		requirements += requirement
	}
	
	private fun setScope(scope: CoroutineScope) {
		this.scope = scope
		scopeSetters.forEach { it(scope) }
	}
	
	@PublishedApi
	internal fun build() = CommandLiteral(buildList {
		val main = builder.requires { source ->
			executes.executors.any { it.condition(source) } && requirements.all { it(source) }
		}.executes(executes.build { scope }).build()
		add(main)
		aliases.mapTo(this) { Commands.literal(it).redirect(main).build() }
	}, if (::description.isInitialized) description else null, ::setScope)
	
	@PublishedApi
	internal fun CommandLiteral.asChild() {
		scopeSetters += setScope
		nodes.forEach { builder.then(it) }
	}
	
	inline fun literal(name: String, vararg aliases: String, action: CommandLiteralContext.() -> Unit) =
		command(name, aliases = aliases, action).asChild()
	
	inline operator fun String.invoke(noinline action: CommandLiteralContext.() -> Unit) =
		literal(this, action = action)
	
	inline operator fun List<String>.invoke(noinline action: CommandLiteralContext.() -> Unit) =
		literal(first(), aliases = drop(1).toTypedArray(), action)
}

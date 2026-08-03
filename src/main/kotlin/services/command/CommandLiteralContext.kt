package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.execution.CommandExecution
import com.github.matin2.mcore.services.command.execution.CommandSourcePredicate
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.ApiStatus

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
	
	@ApiStatus.Internal
	fun build() = CommandLiteral(buildList {
		val main = builder.requires { source ->
			executes.executors.any { it.condition(source) } && requirements.all { it(source) }
		}.executes(executes.build { scope }).build()
		add(main)
		aliases.mapTo(this) { Commands.literal(it).redirect(main).build() }
	}, if (::description.isInitialized) description else null, ::setScope)
	
	inline operator fun String.invoke(action: CommandLiteralContext.() -> Unit) =
		+command(this, action = action)
	
	inline operator fun List<String>.invoke(action: CommandLiteralContext.() -> Unit) =
		+command(first(), aliases = drop(1).toTypedArray(), action)
	
	operator fun CommandLiteral.unaryPlus() {
		scopeSetters += setScope
		nodes.forEach { builder.then(it) }
	}
}

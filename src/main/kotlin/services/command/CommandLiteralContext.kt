package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.execution.CommandExecution
import com.github.matin2.mcore.services.command.execution.Condition
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.ApiStatus

@CommandDsl
@Suppress("NOTHING_TO_INLINE", "unused")
class CommandLiteralContext(name: String, vararg val aliases: String) {
	
	private var scope: CoroutineScope? = null
	private val builder = Commands.literal(name)
	private var requirements: Condition = { true }
	val executes = CommandExecution()
	lateinit var description: String
	
	fun requires(requirement: Condition) {
		val current = requirements
		requirements = { current() && requirement() }
	}
	
	@ApiStatus.Internal
	fun build() = CommandLiteral(buildList {
		val main = builder.requires { requirements(it) && executes.conditions?.invoke(it) != false }
			.executes(executes.build { scope }).build()
		add(main)
		aliases.mapTo(this) { Commands.literal(it).redirect(main).build() }
	}, if (::description.isInitialized) description else null) { scope = it }
	
	inline operator fun String.invoke(action: CommandLiteralContext.() -> Unit) =
		+command(this, action = action)
	
	inline operator fun List<String>.invoke(action: CommandLiteralContext.() -> Unit) =
		+command(first(), aliases = drop(1).toTypedArray(), action)
	
	operator fun CommandLiteral.unaryPlus() = nodes.forEach { builder.then(it) }
}

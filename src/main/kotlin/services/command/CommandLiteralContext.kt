package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.execution.CommandExecution
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.ApiStatus

@CommandDsl
@Suppress("NOTHING_TO_INLINE")
class CommandLiteralContext(name: String, vararg val aliases: String) {
	
	private var scope: CoroutineScope? = null
	private val builder = Commands.literal(name)
	val executes = CommandExecution(::scope.getter)
	lateinit var description: String
	
	@ApiStatus.Internal
	fun build() = CommandLiteral(buildList {
		val main = builder.executes(executes.build()).build()
		add(main)
		aliases.mapTo(this) { Commands.literal(it).redirect(main).build() }
	}, if (::description.isInitialized) description else null, ::scope.setter)
	
	inline operator fun String.invoke(action: CommandLiteralContext.() -> Unit) =
		+command(this, action = action)
	
	inline operator fun List<String>.invoke(action: CommandLiteralContext.() -> Unit) =
		+command(first(), aliases = drop(1).toTypedArray(), action)
	
	operator fun CommandLiteral.unaryPlus() = nodes.forEach { builder.then(it) }
}

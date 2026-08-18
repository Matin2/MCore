package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.CommandExecutor
import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

@Suppress("unused")
class CommandExecution internal constructor(private val scope: CommandCoroutineScope) {
	
	val argument = ExecutionArgument(scope)
	val executes = CommandExecutor()
	
	internal infix fun addTo(builder: ArgumentBuilder<CommandSourceStack, *>) {
		val execution = executes.build(scope)
		val final = argument.list.ifEmpty {
			builder.executes(execution)
			return
		}.apply {
			last().executes(execution)
		}.reduceRightIndexed { index, current, next ->
			current.then(next)
			if (argument.optionalStart != -1 && index + 1 >= argument.optionalStart) current.executes(execution)
			current
		}
		builder.then(final)
		if (argument.optionalStart == 0) builder.executes(execution)
	}
}

package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandSourcePredicate
import com.github.matin2.mcore.services.plugin.Bukkit
import com.github.matin2.mcore.utils.component.component
import com.mojang.brigadier.Command
import com.mojang.brigadier.exceptions.CommandSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.coroutines.CoroutineContext

abstract class CommandExecution {
	
	protected typealias Execution = suspend CommandExecutionContext.() -> Unit
	
	internal val executors = HashSet<Single>()
	
	internal inline fun build(crossinline getScope: () -> CoroutineScope?) = Command { context ->
		val executionContext = CommandExecutionContext(context)
		val executor = executors.find { it.condition(context.source) } ?: return@Command 0
		getScope()?.launch(Dispatchers.Bukkit + executor.context) {
			try {
				executor.execution(executionContext)
			} catch (e: CommandSyntaxException) {
				context.source.sender.sendMessage(e.componentMessage()!!)
			} catch (e: Exception) {
				context.source.sender.sendMessage(component("Failed to execute command.", NamedTextColor.DARK_RED))
				e.printStackTrace()
			}
		}
		1
	}
	
	internal data class Single(
		val context: CoroutineContext,
		val condition: CommandSourcePredicate,
		val execution: Execution
	)
}

package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandService
import com.github.matin2.mcore.services.plugin.Bukkit
import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.CommandNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.key.Key
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused")
class CommandExecution {
	
	private typealias Executor = suspend CommandExecutionContext.() -> Unit
	private typealias PlayerExecutor = suspend CommandExecutionContext.(player: Player) -> Unit
	private typealias ConsoleExecutor = suspend CommandExecutionContext.(console: ConsoleCommandSender) -> Unit
	private typealias Condition = CommandExecutionContext.() -> Boolean
	
	private val executors = HashSet<Single>()
	private lateinit var scope: CoroutineScope
	
	operator fun invoke(
		context: CoroutineContext = EmptyCoroutineContext,
		condition: Condition = { true },
		executor: Executor
	) {
		executors += Single(context, condition, executor)
	}
	
	inline fun player(
		context: CoroutineContext = EmptyCoroutineContext,
		noinline condition: Condition = { source.executor is Player },
		crossinline executor: PlayerExecutor
	) = invoke(context, condition) { executor(source.executor as Player) }
	
	inline fun console(
		context: CoroutineContext = EmptyCoroutineContext,
		noinline condition: Condition = { source.sender is ConsoleCommandSender },
		crossinline executor: ConsoleExecutor
	) = invoke(context, condition) { executor(source.sender as ConsoleCommandSender) }
	
	internal fun build() = Command { context ->
		val executionContext = CommandExecutionContext(context)
		val executor = executors.find { it.condition(executionContext) } ?: return@Command 1
		context.nodes.first().node.getScope()?.launch(Dispatchers.Bukkit + executor.context) {
			executor.executor(executionContext)
		}
		1
	}
	
	@Suppress("NOTHING_TO_INLINE")
	private fun CommandNode<*>.getScope(): CoroutineScope? {
		if (::scope.isInitialized) return scope
		val command = name
		val key = if (command.contains(":")) Key.key(command)
		else Key.key(wrappedCached?.apiCommandMeta?.plugin() ?: return null, command)
		return CommandService[key]?.also { scope = it }
	}
	
	private data class Single(val context: CoroutineContext, val condition: Condition, val executor: Executor)
}

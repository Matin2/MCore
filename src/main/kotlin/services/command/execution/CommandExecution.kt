package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.plugin.Bukkit
import com.github.matin2.mcore.utils.component.component
import com.mojang.brigadier.Command
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

typealias Condition = CommandSourceStack.() -> Boolean

@Suppress("unused")
class CommandExecution {
	
	private typealias Execution = suspend CommandExecutionContext.() -> Unit
	private typealias SourceExecution<Source> = suspend CommandExecutionContext.(Source) -> Unit
	
	internal var conditions: Condition = { true }
	private val executors = HashSet<Single>()
	
	operator fun invoke(
		context: CoroutineContext = EmptyCoroutineContext,
		condition: Condition = { true },
		execution: Execution
	) {
		val current = conditions
		conditions = { current() || condition() }
		executors += Single(context, condition, execution)
	}
	
	inline fun player(
		context: CoroutineContext = EmptyCoroutineContext,
		noinline condition: Condition = { this.executor is Player },
		crossinline execution: SourceExecution<Player>
	) = invoke(context, condition) { execution(source.executor as Player) }
	
	inline fun console(
		context: CoroutineContext = EmptyCoroutineContext,
		noinline condition: Condition = { sender is ConsoleCommandSender },
		crossinline execution: SourceExecution<ConsoleCommandSender>
	) = invoke(context, condition) { execution(source.sender as ConsoleCommandSender) }
	
	inline fun block(
		context: CoroutineContext = EmptyCoroutineContext,
		noinline condition: Condition = { sender is BlockCommandSender },
		crossinline execution: SourceExecution<BlockCommandSender>
	) = invoke(context, condition) { execution(source.sender as BlockCommandSender) }
	
	inline fun entity(
		context: CoroutineContext = EmptyCoroutineContext,
		noinline condition: Condition = { this.executor is Entity },
		crossinline execution: SourceExecution<Entity>
	) = invoke(context, condition) { execution(source.executor as Entity) }
	
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
	
	internal data class Single(val context: CoroutineContext, val condition: Condition, val execution: Execution)
}

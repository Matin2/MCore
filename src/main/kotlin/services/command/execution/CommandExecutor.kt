package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandSourcePredicate
import com.github.matin2.mcore.utils.component.component
import com.mojang.brigadier.Command
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused", "NOTHING_TO_INLINE")
class CommandExecutor internal constructor() {
	
	private typealias Execution = suspend CommandExecution.() -> Unit
	private typealias SourceExecution<Source> = suspend CommandExecution.(Source) -> Unit
	
	private val executors = HashSet<Single>()
	
	operator fun invoke(
		context: CoroutineContext = EmptyCoroutineContext,
		condition: CommandSourcePredicate = { true },
		execution: Execution
	) {
		executors += Single(context, condition, execution)
	}
	
	inline fun player(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline execution: SourceExecution<Player>
	) = entity(context, condition, execution)
	
	inline fun console(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline execution: SourceExecution<ConsoleCommandSender>
	) = invoke(context, { sender is ConsoleCommandSender && condition() }) {
		execution(source.sender as ConsoleCommandSender)
	}
	
	inline fun block(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline execution: SourceExecution<BlockCommandSender>
	) = invoke(context, { sender is BlockCommandSender && condition() }) {
		execution(source.sender as BlockCommandSender)
	}
	
	inline fun <reified E : Entity> entity(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline execution: SourceExecution<E>
	) = invoke(context, { executor is E && condition() }) { execution(source.executor as E) }
	
	internal inline fun build(crossinline getScope: () -> CoroutineScope?) = Command { context ->
		val executor = executors.find { it.condition(context.source) } ?: throw SimpleCommandExceptionType(
			MessageComponentSerializer.message().serialize(component("You can't execute this command"))
		).create()
		getScope()?.launch(executor.context) {
			try {
				executor.execution(CommandExecution(context))
			} catch (e: CommandSyntaxException) {
				context.source.sender.sendMessage(
					e.componentMessage() ?: component(e.rawMessage.string, NamedTextColor.RED)
				)
			} catch (e: Exception) {
				context.source.sender.sendMessage(component("Failed to execute command.", NamedTextColor.RED))
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

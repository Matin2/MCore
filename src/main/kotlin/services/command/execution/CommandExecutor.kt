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
	
	private typealias ExecutionBlock = suspend CommandExecution.() -> Unit
	private typealias SourcedExecutionBlock<Source> = suspend CommandExecution.(Source) -> Unit
	
	private val executors = HashSet<Single>()
	
	operator fun invoke(
		context: CoroutineContext = EmptyCoroutineContext,
		condition: CommandSourcePredicate = { true },
		block: ExecutionBlock
	) {
		executors += Single(context, condition, block)
	}
	
	inline fun player(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline block: SourcedExecutionBlock<Player>
	) = entity(context, condition, block)
	
	inline fun console(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline block: SourcedExecutionBlock<ConsoleCommandSender>
	) = invoke(context, { sender is ConsoleCommandSender && condition() }) {
		block(source.sender as ConsoleCommandSender)
	}
	
	inline fun block(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline block: SourcedExecutionBlock<BlockCommandSender>
	) = invoke(context, { sender is BlockCommandSender && condition() }) {
		block(source.sender as BlockCommandSender)
	}
	
	inline fun <reified E : Entity> entity(
		context: CoroutineContext = EmptyCoroutineContext,
		crossinline condition: CommandSourcePredicate = { true },
		crossinline block: SourcedExecutionBlock<E>
	) = invoke(context, { executor is E && condition() }) { block(source.executor as E) }
	
	internal inline fun build(crossinline getScope: () -> CoroutineScope?) = Command { context ->
		val executor = executors.find { it.condition(context.source) } ?: throw SimpleCommandExceptionType(
			MessageComponentSerializer.message().serialize(component("You can't execute this command"))
		).create()
		getScope()?.launch(executor.context) {
			try {
				executor.block(CommandExecution(context))
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
		val block: ExecutionBlock
	)
}

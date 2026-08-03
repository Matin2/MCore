package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandSourcePredicate
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused")
class CommandLiteralExecution internal constructor() : CommandExecution() {
	
	private typealias SourceExecution<Source> = suspend CommandExecutionContext.(Source) -> Unit
	
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
	) = invoke(context, { executor is Player && condition() }) { execution(source.executor as Player) }
	
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
}

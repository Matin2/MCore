package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandArgumentBuilder
import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.github.matin2.mcore.services.command.argument.AsyncSuggestionHandler.suggest
import com.github.matin2.mcore.services.command.argument.CommandSuggestion
import com.github.matin2.mcore.services.command.argument.CommandSyncSuggestion
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.suggestion.Suggestions
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty

open class Argument<T> internal constructor(
	override val name: String,
	internal val builder: CommandArgumentBuilder<*>,
	internal val index: Int
) : ArgumentHolder<T>

class OptionalArgument<T : Any> internal constructor(name: String, builder: CommandArgumentBuilder<*>, index: Int) :
	Argument<T?>(name, builder, index)

@Suppress("unused")
@CommandDsl
class CommandExecution(
	private val scope: CommandCoroutineScope
) {
	
	private typealias SuggestionBlock<Argument> = suspend CommandSuggestion.(argument: Argument) -> Unit
	
	private var firstOptionalIndex = -1
	private val arguments: MutableList<CommandArgumentBuilder<*>> = []
	
	val executes = CommandExecutor()
	
	fun <T : Any> argument(name: String, type: ArgumentType<T>): Argument<T> {
		val builder = Commands.argument(name, type)
		val index = arguments.size
		arguments += builder
		return Argument(name, builder, index)
	}
	
	@Suppress("NOTHING_TO_INLINE")
	inline operator fun <T : Any> ArgumentType<T>.getValue(thisRef: T?, property: KProperty<*>): Argument<T> =
		argument(property.name, this)
	
	fun <T : Any> Argument<T>.optional(): OptionalArgument<T> {
		if (firstOptionalIndex == -1) firstOptionalIndex = index
		return OptionalArgument(name, builder, index)
	}
	
	fun <T, Arg : Argument<T>> Arg.suggests(context: CoroutineContext, block: SuggestionBlock<Arg>): Arg {
		builder.suggests { commandContext, builder ->
			val future = CompletableFuture<Suggestions>()
			scope()?.suggest(future, commandContext, builder, context) {
				block(this@suggests)
			} ?: error("Scope is unavailable")
			future
		}
		return this
	}
	
	@Suppress("NOTHING_TO_INLINE")
	inline infix fun <T, Arg : Argument<T>> Arg.suggests(noinline block: SuggestionBlock<Arg>): Arg =
		suggests(EmptyCoroutineContext, block)
	
	infix fun <T, Arg : Argument<T>> Arg.suggests(sync: CommandExecution.sync<T, Arg>?): Arg {
		builder.suggests { context, builder ->
			if (sync == null) return@suggests Suggestions.empty()
			CommandSyncSuggestion(builder, context).let { sync.block(it, this) }
			builder.buildFuture()
		}
		return this
	}
	
	internal infix fun addTo(builder: ArgumentBuilder<CommandSourceStack, *>) {
		val execution = executes.build(scope)
		arguments.ifEmpty { builder.executes(execution); return }.last().executes(execution)
		val final = arguments.reduceRightIndexed { index, current, next ->
			current.then(next)
			if (firstOptionalIndex != -1 && index + 1 >= firstOptionalIndex) current.executes(execution)
			current
		}
		builder.then(final)
		if (firstOptionalIndex == 0) builder.executes(execution)
	}
	
	@Suppress("ClassName", "RedundantInnerClassModifier")
	inner class sync<T, Arg : Argument<T>>(internal val block: CommandSyncSuggestion.(argument: Arg) -> Unit)
}

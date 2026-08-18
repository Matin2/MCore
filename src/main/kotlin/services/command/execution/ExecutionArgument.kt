package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandArgumentBuilder
import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.argument.ArgumentHolder
import com.github.matin2.mcore.services.command.argument.AsyncSuggestionHandler.suggest
import com.github.matin2.mcore.services.command.argument.CommandSuggestion
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.suggestion.Suggestions
import io.papermc.paper.command.brigadier.Commands
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.ReadOnlyProperty

@Suppress("unused", "NOTHING_TO_INLINE")
@CommandDsl
class ExecutionArgument internal constructor(private val scope: CommandCoroutineScope) {
	
	typealias SuggestionBlock<T> = suspend CommandSuggestion.(argument: ArgumentHolder<T>) -> Unit
	
	internal val list: List<CommandArgumentBuilder<*>>
		field = mutableListOf()
	internal var optionalStart = -1
		private set
	
	operator fun <T : Any> invoke(name: String, type: ArgumentType<T>): ArgumentHolder<T> {
		list += Commands.argument(name, type)
		return ArgumentHolder(name)
	}
	
	operator fun <T : Any> invoke(
		name: String,
		type: ArgumentType<T>,
		suggestionContext: CoroutineContext = EmptyCoroutineContext,
		suggests: SuggestionBlock<T>
	): ArgumentHolder<T> {
		val argument = ArgumentHolder<T>(name)
		list += Commands.argument(name, type).suggests { commandContext, builder ->
			val future = CompletableFuture<Suggestions>()
			scope()?.suggest(future, commandContext, builder, suggestionContext) {
				suggests(argument)
			} ?: error("Scope is unavailable")
			future
		}
		return argument
	}
	
	operator fun <T : Any> invoke(name: String, type: ArgumentType<T>, suggests: Nothing?): ArgumentHolder<T> {
		list += Commands.argument(name, type).suggests { _, _ -> Suggestions.empty() }
		return ArgumentHolder(name)
	}
	
	fun <T : Any> optional(
		name: String,
		type: ArgumentType<T>,
		suggestionContext: CoroutineContext = EmptyCoroutineContext,
		suggests: SuggestionBlock<T>
	): ArgumentHolder<T?> {
		if (optionalStart == -1) optionalStart = list.size
		invoke(name, type, suggestionContext, suggests)
		return ArgumentHolder(name)
	}
	
	fun <T : Any> optional(name: String, type: ArgumentType<T>): ArgumentHolder<T?> {
		if (optionalStart == -1) optionalStart = list.size
		invoke(name, type)
		return ArgumentHolder(name)
	}
	
	fun <T : Any> optional(name: String, type: ArgumentType<T>, suggests: Nothing?): ArgumentHolder<T?> {
		if (optionalStart == -1) optionalStart = list.size
		invoke(name, type, suggests)
		return ArgumentHolder(name)
	}
	
	inline operator fun <T : Any> invoke(type: ArgumentType<T>): ReadOnlyProperty<Any?, ArgumentHolder<T>> {
		var argument: ArgumentHolder<T>? = null
		return ReadOnlyProperty { ref, property ->
			if (argument == null) argument = invoke(property.name, type)
			argument
		}
	}
	
	inline operator fun <T : Any> invoke(
		type: ArgumentType<T>,
		suggestionContext: CoroutineContext = EmptyCoroutineContext,
		noinline suggests: SuggestionBlock<T>
	): ReadOnlyProperty<Any?, ArgumentHolder<T>> {
		var argument: ArgumentHolder<T>? = null
		return ReadOnlyProperty { ref, property ->
			if (argument == null) argument = invoke(property.name, type, suggestionContext, suggests)
			argument
		}
	}
	
	inline operator fun <T : Any> invoke(
		type: ArgumentType<T>,
		suggests: Nothing?
	): ReadOnlyProperty<Any?, ArgumentHolder<T>> {
		var argument: ArgumentHolder<T>? = null
		return ReadOnlyProperty { ref, property ->
			if (argument == null) argument = invoke(property.name, type, suggests)
			argument
		}
	}
	
	inline fun <T : Any> optional(type: ArgumentType<T>): ReadOnlyProperty<Any?, ArgumentHolder<T?>> {
		var argument: ArgumentHolder<T?>? = null
		return ReadOnlyProperty { ref, property ->
			if (argument == null) argument = optional(property.name, type)
			argument
		}
	}
	
	inline fun <T : Any> optional(
		type: ArgumentType<T>,
		suggestionContext: CoroutineContext = EmptyCoroutineContext,
		noinline suggests: SuggestionBlock<T>
	): ReadOnlyProperty<Any?, ArgumentHolder<T?>> {
		var argument: ArgumentHolder<T?>? = null
		return ReadOnlyProperty { ref, property ->
			if (argument == null) argument = optional(property.name, type, suggestionContext, suggests)
			argument
		}
	}
	
	inline fun <T : Any> optional(
		type: ArgumentType<T>,
		suggests: Nothing?
	): ReadOnlyProperty<Any?, ArgumentHolder<T?>> {
		var argument: ArgumentHolder<T?>? = null
		return ReadOnlyProperty { ref, property ->
			if (argument == null) argument = optional(property.name, type, suggests)
			argument
		}
	}
}

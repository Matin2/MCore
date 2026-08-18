package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandArgumentBuilder
import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.argument.AsyncSuggestionHandler.suggest
import com.github.matin2.mcore.services.command.argument.CommandSuggestion
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.suggestion.Suggestions
import io.papermc.paper.command.brigadier.Commands
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused")
class CommandArgument<T : Any> internal constructor(
	name: String,
	type: ArgumentType<T>,
	override val scope: CommandCoroutineScope
) : CommandPart<CommandArgumentBuilder<T>>() {
	
	override val builder: CommandArgumentBuilder<T> = Commands.argument(name, type)
	
	fun suggests(context: CoroutineContext = EmptyCoroutineContext, block: suspend CommandSuggestion.() -> Unit) {
		builder.suggests { commandContext, builder ->
			val future = CompletableFuture<Suggestions>()
			scope()?.suggest(future, commandContext, builder, context, block) ?: error("Scope is unavailable")
			future
		}
	}
	
	fun suggests(nothing: Nothing?) {
		builder.suggests { _, _ -> Suggestions.empty() }
	}
}

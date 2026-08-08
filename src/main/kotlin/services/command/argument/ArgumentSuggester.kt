package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.command.CommandArgumentBuilder
import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.argument.AsyncSuggestionHandler.suggest
import com.mojang.brigadier.suggestion.Suggestions
import net.kyori.adventure.text.Component
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused")
class ArgumentSuggester(
	val scope: CommandCoroutineScope,
	val builder: CommandArgumentBuilder<*>
) {
	
	operator fun invoke(suggester: CommandSuggestion.() -> Unit) {
		builder.suggests { context, builder ->
			val ctx = object : CommandSuggestion(context, builder) {
				override fun suggest(suggestion: String) {
					builder.suggest(suggestion)
				}
				
				override fun suggest(suggestion: String, tooltip: Component) {
					builder.suggest(suggestion, serializer.serialize(tooltip))
				}
			}
			ctx.suggester()
			builder.buildFuture()
		}
	}
	
	fun async(context: CoroutineContext = EmptyCoroutineContext, suggester: suspend CommandSuggestion.() -> Unit) {
		builder.suggests { commandContext, builder ->
			val future = CompletableFuture<Suggestions>()
			scope()?.suggest(future, commandContext, builder, suggester, context) ?: error("Scope is unavailable")
			future
		}
	}
	
	fun nothing() {
		builder.suggests { _, _ -> Suggestions.empty() }
	}
}

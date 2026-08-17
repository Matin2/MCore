@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.execution.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import kotlinx.coroutines.channels.Channel
import net.kyori.adventure.text.Component
import com.mojang.brigadier.context.CommandContext as BackedContext

@CommandDsl
class CommandSuggestion internal constructor(
	private val channel: Channel<Suggestion>,
	context: BackedContext<CommandSourceStack>,
	builder: SuggestionsBuilder,
	private val range: StringRange
) : CommandContext(context) {
	
	val remaining: String = builder.remaining
	val start: Int = builder.start
	
	private val serializer by lazy { MessageComponentSerializer.message() }
	
	suspend fun suggest(suggestion: String) = channel.send(Suggestion(range, suggestion))
	
	suspend fun suggest(suggestion: String, tooltip: Component) =
		channel.send(Suggestion(range, suggestion, serializer.serialize(tooltip)))
	
	suspend inline fun suggestAll(suggestions: Iterable<String>) = suggestions.forEach { suggest(it) }
	
	suspend inline fun suggestAll(suggestions: Iterable<String>, tooltip: (String) -> Component) =
		suggestions.forEach { suggest(it, tooltip(it)) }
}

@CommandDsl
class CommandSyncSuggestion internal constructor(
	private val builder: SuggestionsBuilder,
	context: BackedContext<CommandSourceStack>,
) : CommandContext(context) {
	val remaining: String = builder.remaining
	
	val start: Int = builder.start
	
	private val serializer by lazy { MessageComponentSerializer.message() }
	
	fun suggest(suggestion: String) {
		builder.suggest(suggestion)
	}
	
	fun suggest(suggestion: String, tooltip: Component) {
		builder.suggest(suggestion, serializer.serialize(tooltip))
	}
	
	inline fun suggestAll(suggestions: Iterable<String>) = suggestions.forEach { suggest(it) }
	inline fun suggestAll(suggestions: Iterable<String>, tooltip: (String) -> Component) =
		suggestions.forEach { suggest(it, tooltip(it)) }
}

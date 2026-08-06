package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.execution.CommandExecutionContext
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.toList
import net.kyori.adventure.text.Component

@CommandDsl
@Suppress("unused", "NOTHING_TO_INLINE")
class CommandSuggestion(
	context: CommandContext<CommandSourceStack>,
	@PublishedApi internal val builder: SuggestionsBuilder
) : CommandExecutionContext(context) {
	
	private val channel = Channel<Suggestion>(BUFFERED)
	private val range = StringRange.between(builder.start, builder.input.length)
	
	inline val remaining: String get() = builder.remaining
	inline val start: Int get() = builder.start
	
	private val serializer by lazy { MessageComponentSerializer.message() }
	
	suspend fun suggest(suggestion: String) {
		channel.send(Suggestion(range, suggestion))
	}
	
	suspend fun suggest(suggestion: String, tooltip: Component) {
		channel.send(Suggestion(range, suggestion, serializer.serialize(tooltip)))
	}
	
	suspend inline fun suggestAll(suggestions: Iterable<String>) = suggestions.forEach { suggest(it) }
	
	suspend inline fun suggestAll(suggestions: Iterable<String>, tooltip: (String) -> Component) =
		suggestions.forEach { suggest(it, tooltip(it)) }
	
	internal inline fun close() {
		channel.close()
	}
	
	internal suspend inline fun get() = Suggestions(range, channel.toList())
}

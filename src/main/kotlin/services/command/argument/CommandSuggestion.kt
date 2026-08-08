package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.command.CommandDsl
import com.github.matin2.mcore.services.command.execution.CommandExecution
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component

@CommandDsl
@Suppress("unused", "NOTHING_TO_INLINE")
abstract class CommandSuggestion internal constructor(
	context: CommandContext<CommandSourceStack>,
	@PublishedApi internal val builder: SuggestionsBuilder,
) : CommandExecution(context) {
	
	inline val remaining: String get() = builder.remaining
	inline val start: Int get() = builder.start
	
	protected val serializer by lazy { MessageComponentSerializer.message() }
	
	abstract fun suggest(suggestion: String)
	
	abstract fun suggest(suggestion: String, tooltip: Component)
	
	inline fun suggestAll(suggestions: Iterable<String>) = suggestions.forEach { suggest(it) }
	
	inline fun suggestAll(suggestions: Iterable<String>, tooltip: (String) -> Component) =
		suggestions.forEach { suggest(it, tooltip(it)) }
}

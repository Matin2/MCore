package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.plugin.Bukkit
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

internal object AsyncSuggestionHandler {
	
	private val suggestions = ConcurrentHashMap<CommandSender, Job>()
	
	fun CoroutineScope.suggest(
		future: CompletableFuture<Suggestions>,
		context: CommandContext<CommandSourceStack>,
		builder: SuggestionsBuilder,
		suggester: suspend CommandSuggestion.() -> Unit,
		extraContext: CoroutineContext
	) {
		suggestions[context.source.sender]?.cancel()
		suggestions[context.source.sender] = launch {
			val channel = Channel<Suggestion>(10)
			val range = StringRange(builder.start, builder.remaining.length)
			val ctx = CommandSuggestion(channel, context, builder, range)
			launch(Dispatchers.Bukkit + extraContext) {
				try {
					ctx.suggester()
				} finally {
					channel.close()
				}
			}
			val suggestions = buildList { channel.consumeEach { add(it) } }
			future.complete(Suggestions(range, suggestions.filter {
				builder.remaining.startsWith(it.text, ignoreCase = true)
			}))
		}
	}
}

package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.plugin.Bukkit
import com.mojang.brigadier.context.CommandContext
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
import kotlin.coroutines.CoroutineContext

internal object AsyncSuggestionHandler {
	
	private val suggestions = HashMap<CommandSender, Job>()
	
	inline fun CoroutineScope.suggest(
		future: CompletableFuture<Suggestions>,
		context: CommandContext<CommandSourceStack>,
		builder: SuggestionsBuilder,
		extraContext: CoroutineContext,
		crossinline block: suspend CommandSuggestion.() -> Unit
	) {
		val sender = context.source.sender
		suggestions[sender]?.cancel()
		val suggestion = launch {
			val channel = Channel<Suggestion>(10)
			val ctx = CommandSuggestion(channel, context, builder)
			launch(Dispatchers.Bukkit + extraContext) {
				try {
					ctx.block()
				} finally {
					channel.close()
				}
			}
			val suggestions = buildList { channel.consumeEach { add(it) } }
			future.complete(Suggestions.create(builder.input, suggestions))
		}
		suggestions[sender] = suggestion
		suggestion.invokeOnCompletion { suggestions.remove(sender, suggestion) }
	}
}

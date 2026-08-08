package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.plugin.Bukkit
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.consumeEach
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

internal object AsyncSuggestionHandler {
	
	private typealias SuggesterBlock = suspend CommandSuggestion.() -> Unit
	
	private val suggesters = ConcurrentHashMap<CommandSender, Channel<Suggester>>()
	
	fun CoroutineScope.suggest(
		future: CompletableFuture<Suggestions>,
		context: CommandContext<CommandSourceStack>,
		builder: SuggestionsBuilder,
		suggester: SuggesterBlock,
		extraContext: CoroutineContext
	) {
		val suggester = Suggester(future, context, builder, suggester, extraContext)
		suggesters[context.source.sender]?.run { trySend(suggester); return }
		val channel = Channel<Suggester>(10)
		suggesters[context.source.sender] = channel
		launch(Dispatchers.IO) {
			var job: Job? = null
			channel.consumeEach { suggester ->
				// TODO Implement Caching
				job?.cancel()
				job = launch {
					val range = StringRange(builder.start, builder.remaining.length)
					val suggestions = suggester.createSuggestions(range)
					suggester.future.complete(Suggestions(range, suggestions.filter {
						builder.remaining.startsWith(it.text, ignoreCase = true)
					}))
				}
			}
		}
		channel.trySend(suggester)
	}
	
	private suspend fun Suggester.createSuggestions(range: StringRange): List<Suggestion> {
		val channel = Channel<Suggestion>(UNLIMITED)
		withContext(Dispatchers.Bukkit + extraContext) {
			val ctx = object : CommandSuggestion(context, builder) {
				override fun suggest(suggestion: String) {
					coroutineContext.ensureActive()
					channel.trySend(Suggestion(range, suggestion))
				}
				
				override fun suggest(suggestion: String, tooltip: Component) {
					coroutineContext.ensureActive()
					channel.trySend(Suggestion(range, suggestion, serializer.serialize(tooltip)))
				}
			}
			try {
				ctx.block()
			} finally {
				channel.close()
			}
		}
		return buildList { channel.consumeEach { add(it) } }
	}
	
	internal data class Suggester(
		val future: CompletableFuture<Suggestions>,
		val context: CommandContext<CommandSourceStack>,
		val builder: SuggestionsBuilder,
		val block: SuggesterBlock,
		val extraContext: CoroutineContext
	)
}

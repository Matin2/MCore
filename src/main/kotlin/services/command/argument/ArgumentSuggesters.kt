package com.github.matin2.mcore.services.command.argument

import com.mojang.brigadier.suggestion.Suggestions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

internal object ArgumentSuggesters {
	
	private val suggesters = HashMap<CommandSender, CompletableFuture<Suggestions>>()
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun CoroutineScope.suggestionFuture(
		sender: CommandSender,
		noinline block: suspend CoroutineScope.() -> Suggestions
	): CompletableFuture<Suggestions> {
		suggesters[sender]?.cancel(true)
		val future = future(Dispatchers.IO, block = block)
		suggesters[sender] = future
		future.thenRun { suggesters.remove(sender) }
		return future
	}
}

package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.argument.ArgumentSuggesters.suggestionFuture
import com.github.matin2.mcore.services.command.argument.CommandSuggestion
import com.github.matin2.mcore.services.plugin.Bukkit
import com.github.matin2.mcore.services.command.CommandArgumentBuilder
import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CommandArgument<T : Any> internal constructor(
	name: String,
	type: ArgumentType<T>,
	override val scope: CommandCoroutineScope
) : CommandPart<CommandArgumentBuilder<T>>() {
	
	override val builder: CommandArgumentBuilder<T> = Commands.argument(name, type)
	
	fun suggests(
		context: CoroutineContext = EmptyCoroutineContext,
		suggester: suspend CommandSuggestion.() -> Unit
	) {
		SuggestionProvider<CommandSourceStack> { commandContext, builder ->
			scope()?.suggestionFuture(commandContext.source.sender) {
				val ctx = CommandSuggestion(commandContext, builder)
				withContext(Dispatchers.Bukkit + context) {
					try {
						ctx.suggester()
					} finally {
						ctx.close()
					}
				}
				ctx.get()
			} ?: error("Scope is unavailable")
		}.let { builder.suggests(it) }
	}
	
	@Suppress("unused")
	fun removeSuggestions() {
		builder.suggests { _, _ -> Suggestions.empty() }
	}
}

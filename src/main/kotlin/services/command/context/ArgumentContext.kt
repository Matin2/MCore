package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.argument.ArgumentSuggesters
import com.github.matin2.mcore.services.command.argument.ArgumentSuggestionContext
import com.github.matin2.mcore.services.plugin.Bukkit
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class ArgumentContext<T : Any> internal constructor(
	name: String,
	type: ArgumentType<T>,
	override val scope: () -> CoroutineScope?,
) : CommandNodeContext<RequiredArgumentBuilder<CommandSourceStack, T>>() {
	
	override val builder = Commands.argument(name, type)
	
	fun suggests(
		context: CoroutineContext = EmptyCoroutineContext,
		suggester: suspend ArgumentSuggestionContext.() -> Unit
	) {
		SuggestionProvider { commandContext, builder ->
			scope()?.future {
				val ctx = ArgumentSuggestionContext(commandContext, builder)
				launch(Dispatchers.Bukkit + context) {
					try {
						ctx.suggester()
					} finally {
						ctx.close()
					}
				}
				ctx.get()
			}?.also { ArgumentSuggesters[commandContext.source.sender] = it } ?: error("")
		}.let { builder.suggests(it) }
	}
	
	@Suppress("unused")
	fun removeSuggestions() {
		builder.suggests { _, _ -> Suggestions.empty() }
	}
}

package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandArgumentBuilder
import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.argument.ArgumentSuggester
import com.mojang.brigadier.arguments.ArgumentType
import io.papermc.paper.command.brigadier.Commands

class CommandArgument<T : Any> internal constructor(
	name: String,
	type: ArgumentType<T>,
	override val scope: CommandCoroutineScope
) : CommandPart<CommandArgumentBuilder<T>>() {
	
	override val builder: CommandArgumentBuilder<T> = Commands.argument(name, type)
	
	val suggests = ArgumentSuggester(scope, builder)
}

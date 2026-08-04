package com.github.matin2.mcore.services.command.context

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

class ArgumentContext<T : Any> internal constructor(
	name: String,
	type: ArgumentType<T>,
	override val scope: () -> CoroutineScope?,
) : CommandNodeContext<RequiredArgumentBuilder<CommandSourceStack, T>>() {
	
	override val builder = Commands.argument(name, type)
}

package com.github.matin2.mcore.services.command.argument

import com.github.matin2.mcore.services.command.CommandContext
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

class ArgumentContext<T : Any> internal constructor(
	name: String,
	type: ArgumentType<T>,
	private val getScope: () -> CoroutineScope?
) : CommandContext<ArgumentContext.LiteralBuilder<T>>() {
	
	private typealias LiteralBuilder<T> = RequiredArgumentBuilder<CommandSourceStack, T>
	
	override val builder: LiteralBuilder<T> = Commands.argument(name, type)
	override val scope get() = getScope()

//	override fun finalizeBuilder(): RequiredArgumentBuilder<CommandSourceStack, *> {
//		val final = super.finalizeBuilder() as RequiredArgumentBuilder<CommandSourceStack, *>
//	}
}

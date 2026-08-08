package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandCoroutineScope
import com.github.matin2.mcore.services.command.CommandLiteralBuilder
import io.papermc.paper.command.brigadier.Commands

@Suppress("NOTHING_TO_INLINE", "unused")
class CommandLiteral internal constructor(
	name: String,
	internal val aliases: Collection<String>,
	override val scope: CommandCoroutineScope
) : CommandPart<CommandLiteralBuilder>() {
	
	override val builder: CommandLiteralBuilder = Commands.literal(name)
}

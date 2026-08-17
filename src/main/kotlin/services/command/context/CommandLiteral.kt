package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandLiteralBuilder
import io.papermc.paper.command.brigadier.Commands

@Suppress("NOTHING_TO_INLINE", "unused")
abstract class CommandLiteral internal constructor(
	name: String,
	internal val aliases: Collection<String>
) : CommandPart<CommandLiteralBuilder>() {
	
	override val builder: CommandLiteralBuilder = Commands.literal(name)
}

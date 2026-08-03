package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.literal.CommandLiteral
import com.github.matin2.mcore.services.command.literal.CommandLiteralContext
import io.papermc.paper.command.brigadier.CommandSourceStack

typealias CommandSourcePredicate = CommandSourceStack.() -> Boolean

fun command(name: String, vararg aliases: String, action: CommandLiteralContext.() -> Unit): CommandLiteral =
	CommandLiteralContext(name, aliases = aliases).apply(action).build()

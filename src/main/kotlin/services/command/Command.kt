package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.literal.CommandLiteral
import com.github.matin2.mcore.services.command.literal.CommandLiteralContext

fun command(name: String, vararg aliases: String, action: CommandLiteralContext.() -> Unit): CommandLiteral =
	CommandLiteralContext(name, aliases.toList()).apply(action).build()

fun command(name: String, aliases: Collection<String>, action: CommandLiteralContext.() -> Unit): CommandLiteral =
	CommandLiteralContext(name, aliases).apply(action).build()

package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.literal.CommandLiteral
import com.github.matin2.mcore.services.command.literal.LiteralContext
import io.papermc.paper.command.brigadier.CommandSourceStack

internal typealias CommandSourcePredicate = @CommandDsl CommandSourceStack.() -> Boolean

fun command(
	name: String,
	aliases: Collection<String>,
	action: LiteralContext.() -> Unit
): CommandLiteral = LiteralContext(name, aliases).run {
	action()
	CommandLiteral(this, finalizeBuilder().build())
}

@Suppress("NOTHING_TO_INLINE")
inline fun command(
	name: String,
	vararg aliases: String,
	noinline action: LiteralContext.() -> Unit
): CommandLiteral = command(name, aliases.toList(), action)

package com.github.matin2.mcore.services.command

import io.papermc.paper.command.brigadier.CommandSourceStack

internal typealias CommandSourcePredicate = CommandSource.() -> Boolean

@Suppress("unused")
@CommandDsl
@JvmInline
value class CommandSource(val source: CommandSourceStack) {
	
	inline val sender get() = source.sender
	inline val executor get() = source.executor
	inline val location get() = source.location
}

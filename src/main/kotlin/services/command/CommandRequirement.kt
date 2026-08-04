package com.github.matin2.mcore.services.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

@Suppress("unused", "NOTHING_TO_INLINE")
class CommandRequirement {
	
	private val requirements: MutableList<CommandSourcePredicate> = []
	
	operator fun invoke(predicate: CommandSourcePredicate) {
		requirements += predicate
	}
	
	inline infix fun restricted(noinline predicate: CommandSourcePredicate) = invoke {
		Commands.restricted(predicate).test(this)
	}
	
	internal inline operator fun invoke(source: CommandSourceStack) = requirements.all { it(source) }
}

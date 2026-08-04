package com.github.matin2.mcore.services.command

import io.papermc.paper.command.brigadier.Commands

@Suppress("unused")
class CommandRequirement {
	
	private val requirements: MutableList<CommandSourcePredicate> = []
	
	operator fun invoke(predicate: CommandSourcePredicate) {
		requirements += predicate
	}
	
	inline infix fun restricted(crossinline predicate: CommandSourcePredicate) = invoke {
		Commands.restricted { predicate(CommandSource(it)) }.test(source)
	}
	
	@Suppress("NOTHING_TO_INLINE")
	internal inline operator fun invoke(source: CommandSource) = requirements.all { it(source) }
}

package com.github.matin2.mcore.services.command

import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import java.util.function.Predicate

@Suppress("unused", "NOTHING_TO_INLINE")
class CommandRequirement {
	
	private lateinit var requirement: Predicate<CommandSourceStack>
	
	operator fun invoke(predicate: CommandSourcePredicate) {
		requirement = Predicate(predicate)
	}
	
	infix fun restricted(predicate: CommandSourcePredicate) {
		requirement = Commands.restricted(predicate)
	}
	
	internal fun <Builder : ArgumentBuilder<CommandSourceStack, Builder>> addTo(builder: Builder): Builder =
		if (::requirement.isInitialized) builder.requires(requirement) else builder
}

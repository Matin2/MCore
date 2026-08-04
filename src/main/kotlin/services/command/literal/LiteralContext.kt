package com.github.matin2.mcore.services.command.literal

import com.github.matin2.mcore.services.command.CommandContext
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@Suppress("NOTHING_TO_INLINE", "unused")
class LiteralContext internal constructor(
	name: String,
	val aliases: Collection<String>
) : CommandContext<LiteralContext.LiteralBuilder>() {
	
	private typealias LiteralBuilder = LiteralArgumentBuilder<CommandSourceStack>
	
	override val builder: LiteralBuilder = Commands.literal(name)
	override var scope: CoroutineScope? = null
		set(value) {
			field = value ?: return
			scopeSetters.forEach { it(value) }
		}
	
	lateinit var description: String
	internal fun getDescription() = if (::description.isInitialized) description else null
}

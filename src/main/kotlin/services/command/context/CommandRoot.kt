package com.github.matin2.mcore.services.command.context

import com.github.matin2.mcore.services.command.CommandLiteralBuilder
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CoroutineScope

@Suppress("NOTHING_TO_INLINE", "unused")
class CommandRoot internal constructor(
	name: String,
	internal val aliases: Collection<String>
) : CommandNodeContext<CommandLiteralBuilder>() {
	
	override val builder: CommandLiteralBuilder = Commands.literal(name)
	
	@Suppress("PropertyName")
	internal var _scope: CoroutineScope? = null
	override val scope = { _scope }
	
	lateinit var description: String
	internal fun getDescription() = if (::description.isInitialized) description else null
}

package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.plugin.KotlinPlugin
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

class CommandLiteral internal constructor(
	internal val context: CommandLiteralContext,
	internal val mainNode: LiteralCommandNode<CommandSourceStack>
) {
	
	@Suppress("unused")
	fun register(plugin: KotlinPlugin) {
		context.scope = plugin
		plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
			commands.registrar().register(mainNode, context.getDescription(), context.aliases.toList())
		}
	}
}

fun command(name: String, vararg aliases: String, action: CommandLiteralContext.() -> Unit): CommandLiteral =
	CommandLiteralContext(name, aliases = aliases).apply(action).build()

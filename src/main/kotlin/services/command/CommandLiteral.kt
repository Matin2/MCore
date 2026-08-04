package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.context.CommandContext
import com.github.matin2.mcore.services.plugin.KotlinPlugin
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

class CommandLiteral internal constructor(
	internal val context: CommandContext,
	internal val node: LiteralCommandNode<CommandSourceStack>
) {
	
	@Suppress("unused")
	fun register(plugin: KotlinPlugin) {
		context._scope = plugin
		plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
			commands.registrar().register(node, context.getDescription(), context.aliases)
		}
	}
}

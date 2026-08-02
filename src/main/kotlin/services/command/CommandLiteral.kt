package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.plugin.KotlinPlugin
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.key.Key

class CommandLiteral(
	internal val nodes: List<LiteralCommandNode<CommandSourceStack>>,
	private val description: String?
) {
	
	@Suppress("unused")
	fun register(plugin: KotlinPlugin) {
		val node = nodes.first()
		plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
			commands.registrar().register(node, description, nodes.drop(1).map { it.name })
		}
		CommandService[Key.key(plugin, node.name)] = plugin
	}
}

inline fun command(name: String, vararg aliases: String, action: CommandLiteralContext.() -> Unit): CommandLiteral =
	CommandLiteralContext(name, aliases = aliases).apply(action).build()

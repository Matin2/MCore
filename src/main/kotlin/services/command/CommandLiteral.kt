package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.plugin.KotlinPlugin
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.CoroutineScope

class CommandLiteral(
	internal val nodes: List<LiteralCommandNode<CommandSourceStack>>,
	private val description: String?,
	private val setScope: (CoroutineScope) -> Unit
) {
	
	@Suppress("unused")
	fun register(plugin: KotlinPlugin) {
		setScope(plugin)
		plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
			commands.registrar().register(nodes.first(), description, nodes.drop(1).map { it.name })
		}
	}
}

inline fun command(name: String, vararg aliases: String, action: CommandLiteralContext.() -> Unit): CommandLiteral =
	CommandLiteralContext(name, aliases = aliases).apply(action).build()

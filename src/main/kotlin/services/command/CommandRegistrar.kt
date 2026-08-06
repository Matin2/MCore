package com.github.matin2.mcore.services.command

import com.github.matin2.mcore.services.command.context.CommandRoot
import com.github.matin2.mcore.services.plugin.KotlinPlugin
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

class CommandRegistrar internal constructor(
	internal val root: CommandRoot,
	internal val node: LiteralCommandNode<CommandSourceStack>
) {
	
	@Suppress("unused")
	fun register(plugin: KotlinPlugin) {
		root._scope = plugin
		plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
			commands.registrar().register(node, root.getDescription(), root.aliases)
		}
	}
}

fun command(
	name: String,
	aliases: Collection<String>,
	block: CommandRoot.() -> Unit
): CommandRegistrar = CommandRoot(name, aliases).run {
	block()
	CommandRegistrar(this, finalize().build())
}

@Suppress("NOTHING_TO_INLINE")
inline fun command(
	name: String,
	vararg aliases: String,
	noinline block: CommandRoot.() -> Unit
): CommandRegistrar = command(name, aliases.toList(), block)

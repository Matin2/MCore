package com.github.matin2.mcore.services.command.execution

import com.github.matin2.mcore.services.command.CommandDsl
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.ParsedCommandNode
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.tree.CommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.jetbrains.annotations.ApiStatus

@Suppress("unused")
@CommandDsl
@JvmInline
value class CommandExecutionContext(@ApiStatus.Internal val context: CommandContext<CommandSourceStack>) {
	
	inline val source: CommandSourceStack get() = context.source
	inline val rootNode: CommandNode<CommandSourceStack> get() = context.rootNode
	inline val nodes: List<ParsedCommandNode<CommandSourceStack>> get() = context.nodes
	inline val input: String get() = context.input
	inline val child: CommandContext<CommandSourceStack> get() = context.child
	inline val lastChild: CommandContext<CommandSourceStack> get() = context.lastChild
	inline val isForked: Boolean get() = context.isForked
	inline val range: StringRange get() = context.range
	inline val redirectModifier: RedirectModifier<CommandSourceStack> get() = context.redirectModifier
}

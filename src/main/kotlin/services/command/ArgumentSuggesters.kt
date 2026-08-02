package com.github.matin2.mcore.services.command

import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

internal object ArgumentSuggesters {
	
	private val suggesters = HashMap<CommandSender, CompletableFuture<*>>()
	
	operator fun set(sender: CommandSender, future: CompletableFuture<*>) {
		suggesters[sender]?.cancel(true)
		suggesters[sender] = future
		future.thenRun { suggesters.remove(sender) }
	}
}

package com.github.matin2.mcore.services.command

import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.key.Key
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

internal object CommandService {
	
	private val scopes = HashMap<Key, CoroutineScope>()
	private val suggesters = HashMap<CommandSender, CompletableFuture<*>>()
	
	operator fun get(key: Key) = scopes[key]
	operator fun set(key: Key, scope: CoroutineScope) {
		scopes[key] = scope
	}
	
	operator fun set(sender: CommandSender, future: CompletableFuture<*>) {
		suggesters[sender]?.cancel(true)
		suggesters[sender] = future
		future.thenRun { suggesters.remove(sender) }
	}
}

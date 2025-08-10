package me.matin.mcore

import com.github.thesilentpro.headdb.api.HeadAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.asDeferred
import me.arcaniax.hdb.api.DatabaseLoadEvent
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.matin.mcore.managers.hook.Hook
import me.matin.mcore.managers.hook.HooksManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin

internal object Hooks {
	
	val manager = HooksManager(MCore.instance, HeadDatabase, HeadDB)
	val skinsRestorer = Hook("SkinsRestorer", false).also { manager.hooks.add(it) }
	
	object HeadDatabase: Hook("HeadDatabase", false) {
		
		val api: CompletableDeferred<HeadDatabaseAPI>? = CompletableDeferred()
			get() = if (available) field else null
		
		@EventHandler
		@Suppress("UnusedReceiverParameter")
		fun DatabaseLoadEvent.onLoad() {
			api?.complete(HeadDatabaseAPI())
		}
	}
	
	object HeadDB: Hook("HeadDB", false) {
		
		private val rsp = Bukkit.getServicesManager().getRegistration(HeadAPI::class.java)
		var api: Deferred<HeadAPI>? = null
			private set
		override val requirements: (Plugin) -> Boolean
			get() = { rsp != null }
		
		override suspend fun onStateChange() = coroutineScope {
			api = if (available) async {
				rsp!!.provider.apply {
					onReady().asDeferred().join()
				}
			} else null
		}
	}
}
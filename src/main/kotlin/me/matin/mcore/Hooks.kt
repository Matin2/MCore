package me.matin.mcore

import com.github.thesilentpro.headdb.api.HeadAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asDeferred
import me.arcaniax.hdb.api.DatabaseLoadEvent
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.matin.mcore.managers.hook.Hook
import me.matin.mcore.managers.hook.HooksManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler

internal object Hooks {
	
	val manager = HooksManager(MCore.instance)
	val skinsRestorer = Hook("SkinsRestorer", false, manager)
	
	object HeadDatabase: Hook("HeadDatabase", false, manager) {
		
		val api: CompletableDeferred<HeadDatabaseAPI>? = CompletableDeferred()
			get() = if (available) field else null
		
		@EventHandler
		@Suppress("UnusedReceiverParameter")
		fun DatabaseLoadEvent.onLoad() {
			api?.complete(HeadDatabaseAPI())
		}
	}
	
	object HeadDB: Hook("HeadDB", false, manager) {
		
		private val rsp = Bukkit.getServicesManager().getRegistration(HeadAPI::class.java)
		val api: Deferred<HeadAPI>? = MCore.pluginScope.async {
			rsp!!.provider.apply {
				onReady().asDeferred().join()
			}
		}
			get() = if (available) field else null
		
		override fun requirements() = rsp != null
	}
}
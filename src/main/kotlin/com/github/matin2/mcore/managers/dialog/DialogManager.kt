package com.github.matin2.mcore.managers.dialog

import com.github.matin2.mcore.MCore
import com.github.matin2.mcore.managers.dialog.context.DialogInputsContext
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import kotlinx.coroutines.launch
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

internal typealias DialogButtonBlock = suspend DialogInputsContext.(Player) -> Unit

@Suppress("UnstableApiUsage")
internal class DialogManager(private val mcore: MCore) : Listener {
	
	init {
		mcore.server.pluginManager.registerEvents(this, mcore)
	}
	
	@EventHandler
	fun PlayerCustomClickEvent.onClick() {
		if (buttonActions.isEmpty()) return
		val context = DialogInputsContext(dialogResponseView ?: return)
		val player = (commonConnection as? PlayerGameConnection)?.player ?: return
		buttonActions.forEach { if (identifier == it.key) mcore.launch { it.value(context, player) } }
	}
	
	companion {
		val buttonActions = HashMap<Key, DialogButtonBlock>()
		val module = module { single<DialogManager>() withOptions { createdAtStart() } }
	}
}

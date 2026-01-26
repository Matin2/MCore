package me.matin.mcore

import me.matin.mcore.managers.hook.HooksHandler
import net.skinsrestorer.api.SkinsRestorer
import net.skinsrestorer.api.SkinsRestorerProvider

internal object Hooks {
	
	@JvmStatic
	var skinsRestorer: SkinsRestorer? = null
		private set
	
	@JvmStatic
	fun HooksHandler.observeHooks() {
		observeHook(
			name = "SkinsRestorer",
			onEnable = { skinsRestorer = SkinsRestorerProvider.get() },
			onDisable = { skinsRestorer = null },
		)
	}
}
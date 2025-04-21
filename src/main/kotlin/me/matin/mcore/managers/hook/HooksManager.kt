package me.matin.mcore.managers.hook

import me.matin.mcore.MCore
import me.matin.mcore.managers.hook.HooksListener.checkHook
import me.matin.mcore.managers.hook.HooksListener.setEnabled
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

open class HooksManager(internal val plugin: Plugin, vararg hooks: Hook) {
	
	val hooks: MutableSet<Hook> = hooks.toMutableSet()
	
	fun newHook(name: String, required: Boolean, versionCheck: (String) -> Boolean = { true }): Hook =
		Hook(name, required, versionCheck).also { hooks.add(it) }
	
	fun manage() {
		HooksListener.managers.add(this)
		hooks.forEach {
			checkHook(it)
			Bukkit.getPluginManager().registerEvents(it, plugin)
		}
		checkRequired {
			allAvailable = "All the required dependencies for ${plugin.name} are installed."
			someUnavailable =
				"${unavailable.joinToString(limit = 3)} are required by ${plugin.name} but are not available."
		}
	}
	
	internal fun checkRequired(message: Messages.() -> Unit) {
		val unavailable = hooks.filter { it.required }.ifEmpty { return }.filter { !it.available }.map { it.name }
		val allAvailable = unavailable.isEmpty()
		val messages = Messages(unavailable).apply(message)
		val message = if (allAvailable) messages.allAvailable else messages.someUnavailable
		plugin setEnabled allAvailable
		message.ifBlank { return }
		MCore.instance.logger.info(message)
	}
	
	internal class Messages(val unavailable: List<String>) {
		
		var allAvailable: String = ""
		var someUnavailable: String = ""
	}
}
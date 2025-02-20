package me.matin.mcore.managers.dependency

import me.matin.mcore.MCore
import me.matin.mcore.managers.dependency.DependencyListener.checkDepend
import me.matin.mcore.managers.dependency.DependencyListener.setEnabled
import org.bukkit.plugin.Plugin

open class DependencyManager(internal val plugin: Plugin) {
	
	internal val dependencies: MutableSet<Dependency> = mutableSetOf()
	
	fun addDependency(
		name: String,
		required: Boolean,
		versionCheck: (String) -> Boolean = { true },
	): Dependency = Dependency(name, required, versionCheck).also { dependencies.add(it) }
	
	fun manage() {
		DependencyListener.managers.add(this)
		val enabled = dependencies.onEach(::checkDepend).filter { it.required }.ifEmpty { return }
			.filter { !it.available }.ifEmpty {
				MCore.instance.logger.info("All the required dependencies for ${plugin.name} are installed.")
				plugin setEnabled true
				return
			}
		MCore.instance.logger.info(
			"${enabled.joinToString(" and ", limit = 3)} are required by ${plugin.name} but are not available."
		)
		plugin setEnabled false
	}
}
package me.matin.mcore.managers.dependency

import me.matin.mcore.managers.dependency.DependencyListener.checkDepend
import me.matin.mcore.managers.dependency.DependencyListener.setEnabled
import me.matin.mcore.methods.async
import me.matin.mlib.filterAll
import org.bukkit.plugin.Plugin

open class DependencyManager(internal val plugin: Plugin) {
	
	internal val dependencies: MutableSet<Dependency> = mutableSetOf()
	
	fun addDependency(
		name: String,
		required: Boolean,
		versionCheck: (String) -> Boolean = { true },
	) = Dependency(name, required, versionCheck).run {
		dependencies.add(this)
		Available()
	}
	
	fun <T: Any> addDependency(
		name: String,
		required: Boolean,
		value: T,
		versionCheck: (String) -> Boolean = { true },
	) = Dependency(name, required, versionCheck).run {
		dependencies.add(this)
		Value(value)
	}
	
	fun manage() = async {
		DependencyListener.managers.add(this)
		val enabled = dependencies.filterAll {
			checkDepend(it)
			it.required to it.available
		}
		plugin.setEnabled(enabled)
	}
}
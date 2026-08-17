package com.github.matin2.mcore.services.command.argument

interface ArgumentHolder<@Suppress("unused") T> {
	
	val name: String
	
	companion object {
		internal operator fun <T : Any> invoke(name: String) = object : ArgumentHolder<T> {
			override val name = name
		}
	}
}

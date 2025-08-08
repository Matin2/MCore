package me.matin.mcore.managers.hook

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Suppress("unused")
sealed class HookCheckEvent(val hook: Hook, val state: CheckState): Event(true) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	enum class CheckState { INITIAL, ENABLED, DISABLED }
	
	companion object {
		
		val handlerList: HandlerList = HandlerList()
	}
}

class HookInitialCheckEvent(hook: Hook): HookCheckEvent(hook, CheckState.INITIAL) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	companion object {
		
		val handlerList: HandlerList = HandlerList()
	}
}

class HookEnableEvent(hook: Hook): HookCheckEvent(hook, CheckState.ENABLED) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	companion object {
		
		val handlerList: HandlerList = HandlerList()
	}
}

class HookDisableEvent(hook: Hook): HookCheckEvent(hook, CheckState.DISABLED) {
	
	override fun getHandlers(): HandlerList = handlerList
	
	companion object {
		
		val handlerList: HandlerList = HandlerList()
	}
}
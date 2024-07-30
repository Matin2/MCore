package me.matin.core.managers.menu.items.button

import me.matin.core.managers.menu.utils.DisplayItem
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import me.matin.core.managers.menu.utils.Interacted as Interact

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Button(
    val slots: Set<Int>,
    vararg val statesDisplay: DisplayItem,
    val show: Boolean = true,
    state: Int = 0,
    val interactAction: Interacted.() -> Unit = {}
) {

    constructor(
        slot: Int,
        vararg statesDisplay: DisplayItem,
        show: Boolean = true,
        state: Int = 0,
        interactAction: Interacted.() -> Unit = {}
    ): this(setOf(slot), *statesDisplay, show = show, state = state, interactAction = interactAction)

    private var stateChangeAction: (StateChanged.() -> Unit)? = null
    lateinit var inventory: Inventory
    val states = statesDisplay.indices.toSet()
    var state: Int = state
        set(value) {
            val oldState = field
            field = when {
                value < 0 -> (value % statesDisplay.size) + statesDisplay.size
                value > states.last() -> value % statesDisplay.size
                else -> value
            }
            slots.forEach {
                inventory.setItem(it, statesDisplay[field].toItem())
            }
            stateChangeAction?.invoke(StateChanged(oldState))
        }

    inner class Interacted(event: InventoryClickEvent, action: ButtonAction): Interact(event, action) {

        val states get() = this@Button.states
        var state: Int
            get() = this@Button.state
            set(value) {
                this@Button.state = value
            }
    }

    inner class StateChanged(val oldState: Int) {

        val states get() = this@Button.states
        var state: Int
            get() = this@Button.state
            set(value) {
                this@Button.state = value
            }
    }

    infix fun onStateChange(action: StateChanged.() -> Unit): Button {
        stateChangeAction = action
        return this
    }
}
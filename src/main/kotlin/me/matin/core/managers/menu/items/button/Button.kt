package me.matin.core.managers.menu.items.button

import me.matin.core.managers.menu.utils.DisplayItem
import me.matin.core.managers.menu.InventoryMenu
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Button(
    val slots: Set<Int>,
    vararg val statesDisplay: DisplayItem,
    val show: Boolean,
    state: Int = 0,
    val interactAction: Interacted.() -> Unit
) {

    constructor(
        slot: Int,
        vararg statesDisplay: DisplayItem,
        show: Boolean,
        state: Int = 0,
        interactAction: Interacted.() -> Unit
    ): this(setOf(slot), *statesDisplay, show = show, state = state, interactAction = interactAction)

    private var stateChangeAction: (StateChanged.() -> Unit)? = null
    lateinit var menu: InventoryMenu
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
                menu.inventory.setItem(it, statesDisplay[field].toItem())
            }
            stateChangeAction?.invoke(StateChanged(oldState))
        }

    @Suppress("DEPRECATION")
    inner class Interacted(private val event: InventoryClickEvent) {

        val view get() = event.view
        val slot get() = event.slot
        val action: ButtonAction by lazy {
            if (event.click == ClickType.NUMBER_KEY) ButtonAction.entries.first { it.hotbar == event.hotbarButton }
            else ButtonAction.entries.first { it.clickType == event.click }
        }
        var cursor: ItemStack
            get() = event.cursor
            set(value) {
                event.setCursor(value)
            }
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
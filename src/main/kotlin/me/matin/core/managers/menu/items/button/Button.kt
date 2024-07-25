package me.matin.core.managers.menu.items.button

import me.matin.core.managers.menu.DisplayItem
import me.matin.core.managers.menu.Menu
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.items.SlotType
import me.matin.core.managers.menu.items.recipe.RecipeItem
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Button(
    val slots: Set<Int>,
    vararg val statesDisplay: DisplayItem,
    state: Int = 0,
    val interactAction: Interacted.() -> Unit
): MenuItem, RecipeItem {

    constructor(
        slot: Int,
        vararg statesDisplay: DisplayItem,
        state: Int = 0,
        interactAction: Interacted.() -> Unit
    ): this(setOf(slot), *statesDisplay, state = state, interactAction = interactAction)

    private var stateChangeAction: Unit? = null
    lateinit var menu: Menu
    val states = statesDisplay.indices
    var state: Int = state
        set(value) {
            field = when {
                value < 0 -> (value % statesDisplay.size) + statesDisplay.size
                value > states.last -> value % statesDisplay.size
                else -> value
            }
            slots.forEach {
                menu.inventory.setItem(it, statesDisplay[field].toItem())
            }
            if (stateChangeAction != null) stateChangeAction
        }

    @Suppress("DEPRECATION")
    inner class Interacted(private val event: InventoryClickEvent) {

        val view get() = event.view
        val slot get() = event.slot
        val slotType by lazy {
            SlotType.entries.first { it.type == event.slotType }
        }
        val action: ButtonAction by lazy {
            if (event.click == ClickType.NUMBER_KEY) ButtonAction.entries.first { it.hotbar == event.hotbarButton }
            else ButtonAction.entries.first { it.clickType == event.click }
        }
        var cursor: ItemStack
            get() = event.cursor
            set(value) {
                event.setCursor(value)
            }
        val states: IntRange get() = this@Button.states
        var state: Int
            get() = this@Button.state
            set(value) {
                this@Button.state = value
            }
    }

    inner class StateChanged {

        val states: IntRange get() = this@Button.states
        var state: Int
            get() = this@Button.state
            set(value) {
                this@Button.state = value
            }
    }

    infix fun onStateChange(action: StateChanged.() -> Unit): Button {
        stateChangeAction = action(StateChanged())
        return this
    }
}
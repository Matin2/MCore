package me.matin.core.managers.menu.items.button

import me.matin.core.managers.menu.items.button.ButtonAction.CLICK.NORMAL
import me.matin.core.managers.menu.items.button.ButtonAction.CLICK.SHIFT
import org.bukkit.event.inventory.ClickType
import org.jetbrains.annotations.Range

@Suppress("unused", "ClassName")
sealed class ButtonAction {

    sealed class CLICK: ButtonAction() {

        sealed class NORMAL: CLICK()

        sealed class SHIFT: CLICK()
    }

    sealed class DROP_KEY: ButtonAction()

    data object LEFT_CLICK: NORMAL()
    data object RIGHT_CLICK: NORMAL()

    data object SHIFT_LEFT_CLICK: SHIFT()
    data object SHIFT_RIGHT_CLICK: SHIFT()

    data object MIDDLE_CLICK: CLICK()
    data object DOUBLE_CLICK: CLICK()

    data object NUMBER_KEY: ButtonAction() {

        internal var theKey: Int = 0
        val key: @Range(from = 1, to = 9) Int get() = theKey
    }

    data object SWAP_OFFHAND: ButtonAction()

    data object DROP: DROP_KEY()

    data object CTRL_DROP: DROP_KEY()

    companion object {

        operator fun get(click: ClickType, hotbar: Int): ButtonAction? = when (click) {
            ClickType.LEFT -> LEFT_CLICK
            ClickType.RIGHT -> RIGHT_CLICK
            ClickType.SHIFT_LEFT -> SHIFT_LEFT_CLICK
            ClickType.SHIFT_RIGHT -> SHIFT_RIGHT_CLICK
            ClickType.MIDDLE -> MIDDLE_CLICK
            ClickType.DOUBLE_CLICK -> DOUBLE_CLICK
            ClickType.DROP -> DROP
            ClickType.CONTROL_DROP -> CTRL_DROP
            ClickType.SWAP_OFFHAND -> SWAP_OFFHAND
            ClickType.NUMBER_KEY -> NUMBER_KEY.apply { theKey = (hotbar + 1) }
            else -> null
        }
    }
}
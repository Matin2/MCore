package me.matin.core.managers.menu.items.button

import org.bukkit.event.inventory.ClickType

@Suppress("unused", "ClassName")
sealed class ButtonAction {

    sealed class CLICK: ButtonAction() {

        sealed class NORMAL: CLICK() {

            data object LEFT: NORMAL()
            data object RIGHT: NORMAL()

            companion object: NORMAL()
        }

        sealed class SHIFT: CLICK() {

            data object LEFT: SHIFT()
            data object RIGHT: SHIFT()

            companion object: SHIFT()
        }

        data object MIDDLE: CLICK()

        data object DOUBLE: CLICK()

        companion object: CLICK()
    }

    sealed class NUMBER_KEY(vararg val key: Int): ButtonAction() {

        data object KEY_1: NUMBER_KEY(1)
        data object KEY_2: NUMBER_KEY(2)
        data object KEY_3: NUMBER_KEY(3)
        data object KEY_4: NUMBER_KEY(4)
        data object KEY_5: NUMBER_KEY(5)
        data object KEY_6: NUMBER_KEY(6)
        data object KEY_7: NUMBER_KEY(7)
        data object KEY_8: NUMBER_KEY(8)
        data object KEY_9: NUMBER_KEY(9)

        companion object: NUMBER_KEY(1, 2, 3, 4, 5, 6, 7, 8, 9)
    }

    data object SWAP_OFFHAND: ButtonAction()

    sealed class DROP_KEY: ButtonAction() {

        data object NORMAL: DROP_KEY()
        data object WITH_CTRL_KEY: DROP_KEY()

        companion object: DROP_KEY()
    }

    companion object {

        operator fun get(click: ClickType, hotbar: Int?): ButtonAction? = when (click) {
            ClickType.LEFT -> CLICK.NORMAL.LEFT
            ClickType.SHIFT_LEFT -> CLICK.SHIFT.LEFT
            ClickType.RIGHT -> CLICK.NORMAL.RIGHT
            ClickType.SHIFT_RIGHT -> CLICK.SHIFT.LEFT
            ClickType.MIDDLE -> CLICK.MIDDLE
            ClickType.DOUBLE_CLICK -> CLICK.DOUBLE
            ClickType.DROP -> DROP_KEY.NORMAL
            ClickType.CONTROL_DROP -> DROP_KEY.WITH_CTRL_KEY
            ClickType.SWAP_OFFHAND -> SWAP_OFFHAND
            ClickType.NUMBER_KEY -> when (hotbar) {
                0 -> NUMBER_KEY.KEY_1
                1 -> NUMBER_KEY.KEY_2
                2 -> NUMBER_KEY.KEY_3
                3 -> NUMBER_KEY.KEY_4
                4 -> NUMBER_KEY.KEY_5
                5 -> NUMBER_KEY.KEY_6
                6 -> NUMBER_KEY.KEY_7
                7 -> NUMBER_KEY.KEY_8
                8 -> NUMBER_KEY.KEY_9
                else -> null
            }

            else -> null
        }
    }
}
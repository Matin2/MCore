package me.matin.core.managers.menu.items.button

import org.bukkit.event.inventory.ClickType

@Suppress("unused", "ClassName")
sealed class ButtonAction {

    sealed class CLICK: ButtonAction() {

        sealed class NORMAL: CLICK() {

            data object LEFT: NORMAL()
            data object RIGHT: NORMAL()
        }

        sealed class SHIFT: CLICK() {

            data object LEFT: SHIFT() {

                override fun toString(): String = "SHIFT_LEFT"
            }

            data object RIGHT: SHIFT() {

                override fun toString(): String = "SHIFT_RIGHT"
            }
        }

        data object MIDDLE: CLICK()

        data object DOUBLE: CLICK()
    }

    sealed class NUMBER_KEY: ButtonAction() {

        data object KEY_1: NUMBER_KEY()
        data object KEY_2: NUMBER_KEY()
        data object KEY_3: NUMBER_KEY()
        data object KEY_4: NUMBER_KEY()
        data object KEY_5: NUMBER_KEY()
        data object KEY_6: NUMBER_KEY()
        data object KEY_7: NUMBER_KEY()
        data object KEY_8: NUMBER_KEY()
        data object KEY_9: NUMBER_KEY()
    }

    data object SWAP_OFFHAND: ButtonAction()

    sealed class DROP_KEY: ButtonAction() {

        data object NORMAL: DROP_KEY() {

            override fun toString(): String = "DROP"
        }

        data object WITH_CTRL_KEY: DROP_KEY() {

            override fun toString(): String = "CTRL_DROP"
        }
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
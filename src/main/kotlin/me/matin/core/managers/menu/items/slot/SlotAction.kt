package me.matin.core.managers.menu.items.slot

import org.bukkit.event.inventory.InventoryAction
import org.jetbrains.annotations.Range

@Suppress("ClassName")
sealed class SlotAction {

    sealed class PLACE: SlotAction() {

        data object ONE: PLACE() {

            override fun toString(): String = "PLACE_ONE"
        }

        data object SOME: PLACE() {

            override fun toString(): String = "PLACE_SOME"
        }

        data object ALL: PLACE() {

            override fun toString(): String = "PLACE_ALL"
        }
    }

    sealed class PICKUP: SlotAction() {

        data object ONE: PICKUP() {

            override fun toString(): String = "PICKUP_ONE"
        }

        data object HALF: PICKUP() {

            override fun toString(): String = "PICKUP_HALF"
        }

        data object ALL: PICKUP() {

            override fun toString(): String = "PICKUP_ALL"
        }
    }

    sealed class DROP: SlotAction() {

        data object ONE: PICKUP() {

            override fun toString(): String = "DROP_ONE"
        }

        data object ALL: PICKUP() {

            override fun toString(): String = "DROP_ALL"
        }
    }

    sealed class HOTBAR_SWAP: SlotAction() {

        companion object: HOTBAR_SWAP() {

            internal var mutableSlot: Int = 0
            val slot: @Range(from = 0, to = 8) Int get() = mutableSlot
        }
    }

    data object OFF_HAND_SWAP: HOTBAR_SWAP()

    data object CURSOR_SWAP: SlotAction()

    data object MOVE_TO_OTHER_INVENTORY: SlotAction()

    companion object {

        operator fun get(action: InventoryAction, hotbar: Int, offhand: Boolean): SlotAction? = when (action) {
            InventoryAction.PICKUP_ALL -> PICKUP.ALL
            InventoryAction.PICKUP_HALF -> PICKUP.HALF
            InventoryAction.PICKUP_ONE -> PICKUP.ONE
            InventoryAction.PLACE_ALL -> PLACE.ALL
            InventoryAction.PLACE_ONE -> PLACE.ONE
            InventoryAction.PLACE_SOME -> PLACE.SOME
            InventoryAction.SWAP_WITH_CURSOR -> CURSOR_SWAP
            InventoryAction.DROP_ALL_SLOT -> DROP.ALL
            InventoryAction.DROP_ONE_SLOT -> DROP.ONE
            InventoryAction.MOVE_TO_OTHER_INVENTORY -> MOVE_TO_OTHER_INVENTORY
            InventoryAction.HOTBAR_SWAP -> when (offhand) {
                true -> OFF_HAND_SWAP
                false -> HOTBAR_SWAP.apply { mutableSlot = hotbar }
            }

            else -> null
        }
    }
}
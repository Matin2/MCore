package me.matin.core.managers.menu.items.slot

import org.bukkit.event.inventory.InventoryAction
import org.jetbrains.annotations.Range

@Suppress("ClassName")
sealed class SlotAction {

    sealed class PLACE: SlotAction()

    sealed class PICKUP: SlotAction()

    sealed class DROP: SlotAction()

    data object PLACE_ONE: PLACE()
    data object PLACE_SOME: PLACE()
    data object PLACE_ALL: PLACE()

    data object PICKUP_ONE: PICKUP()
    data object PICKUP_HALF: PICKUP()
    data object PICKUP_ALL: PICKUP()

    data object DROP_ONE: DROP()
    data object DROP_ALL: DROP()

    data object HOTBAR_SWAP: SlotAction() {

        internal var mutableSlot: Int = 0
        val slot: @Range(from = 0, to = 8) Int get() = mutableSlot
    }

    data object OFF_HAND_SWAP: SlotAction()

    data object CURSOR_SWAP: SlotAction()

    data object MOVE_TO_OTHER_INVENTORY: SlotAction()

    companion object {

        operator fun get(action: InventoryAction, hotbar: Int, offhand: Boolean): SlotAction? = when (action) {
            InventoryAction.PICKUP_ALL -> PICKUP_ALL
            InventoryAction.PICKUP_HALF -> PICKUP_HALF
            InventoryAction.PICKUP_ONE -> PICKUP_ONE
            InventoryAction.PLACE_ALL -> PLACE_ALL
            InventoryAction.PLACE_SOME -> PLACE_SOME
            InventoryAction.PLACE_ONE -> PLACE_ONE
            InventoryAction.SWAP_WITH_CURSOR -> CURSOR_SWAP
            InventoryAction.DROP_ALL_SLOT -> DROP_ALL
            InventoryAction.DROP_ONE_SLOT -> DROP_ONE
            InventoryAction.MOVE_TO_OTHER_INVENTORY -> MOVE_TO_OTHER_INVENTORY
            InventoryAction.HOTBAR_SWAP -> when (offhand) {
                true -> OFF_HAND_SWAP
                false -> HOTBAR_SWAP.apply { mutableSlot = hotbar }
            }

            else -> null
        }
    }
}
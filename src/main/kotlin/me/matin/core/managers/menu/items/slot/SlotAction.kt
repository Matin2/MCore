package me.matin.core.managers.menu.items.slot

import org.bukkit.event.inventory.InventoryAction

@Suppress("ClassName")
sealed class SlotAction {

    sealed class PLACE: SlotAction() {
        sealed class CURSOR: PLACE()
    }

    sealed class PICKUP: SlotAction() {
        sealed class CURSOR: PICKUP()
    }

    sealed class SWAP: SlotAction()

    sealed class DROP: SlotAction()

    data object PLACE_ONE: PLACE.CURSOR()
    data object PLACE_SOME: PLACE.CURSOR()
    data object PLACE_ALL: PLACE.CURSOR()

    data object PICKUP_ONE: PICKUP.CURSOR()
    data object PICKUP_HALF: PICKUP.CURSOR()
    data object PICKUP_ALL: PICKUP.CURSOR()

    data object DROP_ONE: DROP()
    data object DROP_ALL: DROP()

    data object CURSOR_SWAP: SWAP()

    data object INVENTORY_PLACE: PLACE() {

        internal var slotSetter: Int = 0
        val slot: Int get() = slotSetter
    }

    data object INVENTORY_PICKUP: PICKUP() {

        internal var slotSetter: Int = 0
        val slot: Int get() = slotSetter
    }

    data object INVENTORY_SWAP: SWAP() {

        internal var slotSetter: Int = 0
        val slot: Int get() = slotSetter
    }

    companion object {

        operator fun get(action: InventoryAction): SlotAction? = when (action) {
            InventoryAction.PICKUP_ALL -> PICKUP_ALL
            InventoryAction.PICKUP_HALF -> PICKUP_HALF
            InventoryAction.PICKUP_ONE -> PICKUP_ONE
            InventoryAction.PLACE_ALL -> PLACE_ALL
            InventoryAction.PLACE_SOME -> PLACE_SOME
            InventoryAction.PLACE_ONE -> PLACE_ONE
            InventoryAction.SWAP_WITH_CURSOR -> CURSOR_SWAP
            InventoryAction.DROP_ALL_SLOT -> DROP_ALL
            InventoryAction.DROP_ONE_SLOT -> DROP_ONE
            else -> null
        }
    }
}
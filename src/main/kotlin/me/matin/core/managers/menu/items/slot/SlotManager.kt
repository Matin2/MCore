package me.matin.core.managers.menu.items.slot

import me.matin.core.Core
import org.bukkit.Material
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory

@Suppress("MemberVisibilityCanBePrivate")
class SlotManager(private val inventory: Inventory) {

    val slots = mutableSetOf<Slot>()

    fun manageBehavior(event: InventoryInteractEvent) = when (event) {
        is InventoryDragEvent -> manageDrag(event)
        is InventoryClickEvent -> when {
            event.clickedInventory == event.whoClicked.openInventory.topInventory -> manageClick(event)
            else -> manageBottom(event)
        }

        else -> {}
    }

    private fun manageClick(event: InventoryClickEvent) {
        val slot = slots.firstOrNull { it.slot == event.slot && it.show } ?: return
        val manager = object: ManageClick {
            override val event: InventoryClickEvent = event
            override val slot: Slot = slot
        }
        val action =
            if (slot.display.material == Material.AIR) manager.manageSlotAction()
            else manager.manageDisplayAction()
        slot.interactAction(slot.Interacted(event, action ?: return))
    }

    private interface ManageClick {

        val event: InventoryClickEvent
        val slot: Slot

        @Suppress("DEPRECATION")
        fun manageDisplayAction(): SlotAction? {
            if (event.currentItem != slot.display.toItem()) return manageSlotAction()
            return when (event.action) {
                InventoryAction.SWAP_WITH_CURSOR -> {
                    if (!slot.itemPredicate(event.cursor)) return null
                    event.setCursor(null)
                    SlotAction.PLACE.ALL
                }

                InventoryAction.HOTBAR_SWAP -> manageHotbarSwap(true)
                else -> {
                    event.isCancelled = true
                    return null
                }
            }
        }

        fun manageSlotAction(): SlotAction? {
            val action = SlotAction[event.action, event.hotbarButton, event.click == ClickType.SWAP_OFFHAND]
            action ?: run {
                event.isCancelled = true
                return null
            }
            when (action) {
                SlotAction.PLACE -> if (managePlace()) return null
                SlotAction.PICKUP, SlotAction.DROP, SlotAction.MOVE_TO_OTHER_INVENTORY -> if (managePickup()) return null
                SlotAction.CURSOR_SWAP -> if (managePlace() && managePickup()) return null
                SlotAction.HOTBAR_SWAP, SlotAction.OFF_HAND_SWAP -> manageHotbarSwap(false)
                else -> {}
            }
            return action
        }

        private fun managePlace(): Boolean {
            event.currentItem!!.takeIf { slot.itemPredicate(it) } ?: run {
                event.isCancelled = true
                return true
            }
            return false
        }

        private fun managePickup(): Boolean {
            if (!slot.allowTake) event.isCancelled = true
            return !slot.allowTake
        }

        private fun manageHotbarSwap(makeNull: Boolean): SlotAction? {
            val swapSlot = if (event.click != ClickType.SWAP_OFFHAND) event.hotbarButton else 40
            val inv = event.whoClicked.inventory
            inv.getItem(swapSlot)?.takeUnless { it.amount == 0 || it.type == Material.AIR || !slot.itemPredicate(it) }
                ?: run {
                    event.isCancelled = true
                    return null
                }
            if (makeNull) inv.setItem(swapSlot, null)
            return if (event.click != ClickType.SWAP_OFFHAND) SlotAction.HOTBAR_SWAP.apply {
                mutableSlot = event.hotbarButton
            }
            else SlotAction.OFF_HAND_SWAP
        }
    }

    private fun manageBottom(event: InventoryClickEvent) {
        event.isCancelled = true
        val item = event.currentItem?.takeUnless { it.type == Material.AIR || it.amount == 0 } ?: return
        Core.scheduleTask(true) {
            slots.sortedBy { it.slot }.firstOrNull { slot -> slot.itemPredicate(item) && slot.show }?.also {
                it.item = event.currentItem
                Core.scheduleTask {
                    it.interactAction(it.Interacted(event, SlotAction.MOVE_TO_OTHER_INVENTORY))
                }
            }
        }
    }

    private fun manageDrag(event: InventoryDragEvent) {}

    fun manageDisplay(fillerSlots: MutableSet<Int>, useDefaultItem: Boolean) {
        slots.forEach {
            if (!it.show) return
            it.inventory = inventory
            val item = if (useDefaultItem && it.defaultItem != null) it.defaultItem else it.display.toItem()
            inventory.setItem(it.slot, item)
            fillerSlots.remove(it.slot)
        }
    }
}
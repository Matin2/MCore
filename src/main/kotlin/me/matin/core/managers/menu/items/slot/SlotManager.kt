package me.matin.core.managers.menu.items.slot

import me.matin.core.Core
import org.bukkit.Material
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

@Suppress("MemberVisibilityCanBePrivate")
class SlotManager {

    val slots = mutableSetOf<Slot>()

    fun manageBehavior(event: InventoryInteractEvent) {
        when (event) {
            is InventoryDragEvent -> manageDrag(event)
            is InventoryClickEvent -> when {
                event.clickedInventory == event.whoClicked.openInventory.topInventory -> manageClick(event)
                else -> manageBottom(event)
            }

            else -> {}
        }
    }

    private fun manageClick(event: InventoryClickEvent) {
        val slot = slots.firstOrNull { it.show && it.slot == event.slot } ?: return
        val manager = object: ManageClick {
            override val event: InventoryClickEvent = event
            override val slot: Slot = slot
        }
        val action =
            if (slot.display.material == Material.AIR || event.currentItem != slot.display.toItem()) manager.manageSlotAction()
            else manager.manageDisplayAction()
        slot.interactAction(slot.Interacted(event, action ?: return))
    }

    private interface ManageClick {

        val event: InventoryClickEvent
        val slot: Slot

        @Suppress("DEPRECATION")
        fun manageDisplayAction(): SlotAction? {
            return when (event.action) {
                InventoryAction.HOTBAR_SWAP -> manageHotbarSwap(true, null)
                InventoryAction.SWAP_WITH_CURSOR -> {
                    if (!slot.itemPredicate(event.cursor)) return disAllow()
                    event.setCursor(ItemStack(Material.AIR))
                    SlotAction.PLACE.ALL
                }

                else -> disAllow()
            }
        }

        fun manageSlotAction(): SlotAction? {
            val action =
                SlotAction[event.action, event.hotbarButton, event.click == ClickType.SWAP_OFFHAND] ?: return null
            val oldItem = event.currentItem?.takeUnless { it.amount == 0 || it.type == Material.AIR }
            when (action) {
                SlotAction.PLACE -> if (!slot.itemPredicate(event.cursor)) return disAllow()
                SlotAction.PICKUP, SlotAction.DROP, SlotAction.MOVE_TO_OTHER_INVENTORY -> if (!slot.allowTake) return disAllow()
                SlotAction.CURSOR_SWAP -> if (!slot.itemPredicate(event.cursor) && !slot.allowTake) return disAllow()
                SlotAction.HOTBAR_SWAP, SlotAction.OFF_HAND_SWAP -> manageHotbarSwap(false, oldItem)
                else -> {}
            }
            return action
        }

        private fun manageHotbarSwap(isOldItemDisplay: Boolean, oldItem: ItemStack?): SlotAction? {
            val swapSlot = if (event.click != ClickType.SWAP_OFFHAND) event.hotbarButton else 40
            val inv = event.whoClicked.inventory as Inventory
            val newItem = inv.getItem(swapSlot)?.takeUnless { it.amount == 0 || it.type == Material.AIR }
            when (HotBarCheck[newItem, oldItem, isOldItemDisplay]) {
                HotBarCheck.DISPLAY_PICKUP -> slot.allowTake.takeIf { it }?.also {
                    inv.setItem(swapSlot, ItemStack(Material.AIR))
                } ?: return disAllow()

                HotBarCheck.DISPLAY_PLACE -> slot.itemPredicate(newItem!!).takeIf { it }?.also {
                    inv.setItem(swapSlot, ItemStack(Material.AIR))
                } ?: return disAllow()

                HotBarCheck.PICKUP -> slot.allowTake.takeIf { it } ?: return disAllow()
                HotBarCheck.PLACE -> slot.itemPredicate(newItem!!).takeIf { it } ?: return disAllow()
                HotBarCheck.SWAP -> slot.itemPredicate(newItem!!).takeIf { it && slot.allowTake } ?: return disAllow()
                null -> {}
            }
            return event.click.takeIf { it == ClickType.SWAP_OFFHAND }?.run { SlotAction.OFF_HAND_SWAP } ?: run {
                SlotAction.HOTBAR_SWAP.apply { mutableSlot = event.hotbarButton }
            }
        }

        private enum class HotBarCheck { PLACE, PICKUP, SWAP, DISPLAY_PLACE, DISPLAY_PICKUP;

            companion object {

                operator fun get(newItem: ItemStack?, oldItem: ItemStack?, isOldItemDisplay: Boolean): HotBarCheck? =
                    when {
                        isOldItemDisplay && newItem != null -> DISPLAY_PLACE
                        isOldItemDisplay && newItem == null -> DISPLAY_PICKUP
                        oldItem == null && newItem != null -> PLACE
                        oldItem != null && newItem == null -> PICKUP
                        oldItem != null && newItem != null -> SWAP
                        else -> null
                    }
            }
        }

        private fun disAllow(): SlotAction? {
            event.isCancelled = true
            return null
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

    private fun manageDrag(event: InventoryDragEvent) = Core.scheduleTask {
        val itemAmount = if (event.type == DragType.SINGLE) 1 else run {
            val placedAmount = event.oldCursor.amount - (event.cursor?.amount ?: 0)
            placedAmount / event.rawSlots.size
        }
        val topInv = event.whoClicked.openInventory.topInventory
        val topSlots = event.rawSlots.filterNotNull().filter { it < topInv.size }
        val item = event.oldCursor.apply { amount = itemAmount }
        val customSlots = slots.filter { it.slot in topSlots && it.itemPredicate(item) && it.show }
        val /* Amount of */ notAllowedSlots =
            getNotAllowedSlots(topInv, topSlots.toSet(), customSlots.map { it.slot }.toSet())
        event.cursor = event.oldCursor.apply { amount = (event.rawSlots.size - notAllowedSlots) * itemAmount }
        val action: SlotAction = when {
            event.cursor == null -> SlotAction.PLACE.ALL
            itemAmount == 1 -> SlotAction.PLACE.ONE
            else -> SlotAction.PLACE.SOME
        }
        customSlots.forEach { it.interactAction(it.Interacted(event, action)) }
    }

    private fun getNotAllowedSlots(inventory: Inventory, topSlots: Set<Int>, allowedSlots: Set<Int>): Int {
        val notAllowedSlots = topSlots - allowedSlots
        notAllowedSlots.forEach {
            inventory.setItem(it, null)
        }
        return notAllowedSlots.size
    }

    fun manageDisplay(inventory: Inventory, fillerSlots: MutableSet<Int>, useDefaultItem: Boolean) {
        slots.forEach { slot ->
            if (!slot.show) {
                slot.item?.also { slot.itemDeleteAction?.invoke(it, ItemDeleteReason.SLOT_WAS_HIDDEN) }
                return
            }
            slot.inventory = inventory
            val item = if (useDefaultItem && slot.defaultItem != null) slot.defaultItem else slot.display.toItem()
            inventory.setItem(slot.slot, item)
            fillerSlots.remove(slot.slot)
        }
    }
}
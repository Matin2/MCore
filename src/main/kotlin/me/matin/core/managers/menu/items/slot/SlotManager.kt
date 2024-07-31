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
//            is InventoryDragEvent -> manageDrag(event)
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
        val (action, oldItem) =
            if (slot.display.material == Material.AIR || event.currentItem != slot.display.toItem()) manager.manageSlotAction()
            else manager.manageDisplayAction()
        action ?: return
        slot.item = event.currentItem?.takeUnless {
            it == slot.display.toItem() || it.type == Material.AIR || it.amount == 0
        }
        slot.mutableOldItem = oldItem
        event.whoClicked.sendMessage("OldItem: ${slot.oldItem?.type}\nNewItem: ${slot.item?.type}")
        slot.interactAction(slot.Interacted(event, action))
    }

    private interface ManageClick {

        val event: InventoryClickEvent
        val slot: Slot

        @Suppress("DEPRECATION")
        fun manageDisplayAction(): Pair<SlotAction?, ItemStack?> {
            return when (event.action) {
                InventoryAction.HOTBAR_SWAP -> manageHotbarSwap(true, null)
                InventoryAction.SWAP_WITH_CURSOR -> {
                    if (!slot.itemPredicate(event.cursor)) return disAllow()
                    (SlotAction.PLACE.ALL to event.cursor).also {
                        event.setCursor(ItemStack(Material.AIR))
                    }
                }

                else -> disAllow()
            }
        }

        fun manageSlotAction(): Pair<SlotAction?, ItemStack?> {
            val action =
                SlotAction[event.action, event.hotbarButton, event.click == ClickType.SWAP_OFFHAND]
                    ?: return null to null
            val oldItem = event.currentItem?.takeUnless { it.amount == 0 || it.type == Material.AIR }
            when (action) {
                is SlotAction.PLACE -> if (!slot.itemPredicate(event.cursor)) return disAllow()
                is SlotAction.PICKUP, is SlotAction.DROP, is SlotAction.MOVE_TO_OTHER_INVENTORY -> if (!slot.allowTake) return disAllow()
                is SlotAction.CURSOR_SWAP -> if (!slot.itemPredicate(event.cursor) && !slot.allowTake) return disAllow()
                is SlotAction.HOTBAR_SWAP, is SlotAction.OFF_HAND_SWAP -> manageHotbarSwap(false, oldItem)
            }
            return action to event.cursor
        }

        private fun manageHotbarSwap(isOldItemDisplay: Boolean, oldItem: ItemStack?): Pair<SlotAction?, ItemStack?> {
            val swapSlot = if (event.click != ClickType.SWAP_OFFHAND) event.hotbarButton else 40
            val inv = event.whoClicked.inventory as Inventory
            val newItem = inv.getItem(swapSlot)?.takeUnless { it.amount == 0 || it.type == Material.AIR }
            when (HotBarCheck[newItem, oldItem, isOldItemDisplay]) {
                HotBarCheck.DISPLAY_PICKUP -> return disAllow()
                HotBarCheck.DISPLAY_PLACE -> slot.itemPredicate(newItem!!).takeIf { it }?.also {
                    inv.setItem(swapSlot, ItemStack(Material.AIR))
                } ?: return disAllow()
                HotBarCheck.PLACE -> slot.itemPredicate(newItem!!).takeIf { it } ?: return disAllow()
                HotBarCheck.PICKUP -> slot.allowTake.takeIf { it } ?: return disAllow()
                HotBarCheck.SWAP -> slot.itemPredicate(newItem!!).takeIf { it && slot.allowTake } ?: return disAllow()
                null -> {}
            }
            return (event.click.takeIf { it == ClickType.SWAP_OFFHAND }?.run { SlotAction.OFF_HAND_SWAP } ?: run {
                SlotAction.HOTBAR_SWAP.apply { mutableSlot = event.hotbarButton }
            }) to newItem
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

        private fun disAllow(): Pair<SlotAction?, ItemStack?> {
            event.isCancelled = true
            return null to null
        }
    }

    private fun manageBottom(event: InventoryClickEvent) {
        if (event.action != InventoryAction.MOVE_TO_OTHER_INVENTORY) return
        event.isCancelled = true
        val item = event.currentItem?.takeUnless { it.type == Material.AIR || it.amount == 0 } ?: return
        Core.scheduleTask(true) {
            slots.sortedBy { it.slot }.firstOrNull { it.show && it.itemPredicate(item) }?.also {
                it.item = item
                Core.scheduleTask {
                    it.interactAction(it.Interacted(event, SlotAction.MOVE_TO_OTHER_INVENTORY))
                }
            }
        }
    }

    private fun manageDrag(event: InventoryDragEvent) = Core.scheduleTask(true) {
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
            val item = if (useDefaultItem && slot.item != null) slot.item else slot.display.toItem()
            inventory.setItem(slot.slot, item)
            fillerSlots.remove(slot.slot)
        }
    }
}
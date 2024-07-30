package me.matin.core.managers.menu.items.slot

import me.matin.core.managers.menu.utils.DisplayItem
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("unused")
class Slot(
    val slot: Int,
    val display: DisplayItem,
    val defaultItem: ItemStack? = null,
    val show: Boolean = true,
    val allowTake: Boolean = true,
    val itemPredicate: (ItemStack) -> Boolean = { true },
    val itemDeleteAction: ((item: ItemStack, reason: ItemDeleteReason) -> Unit)? = null,
    val interactAction: Interacted.() -> Unit = {}
) {

    lateinit var inventory: Inventory
    var oldItem: ItemStack? = null
    var item: ItemStack?
        get() {
            return inventory.getItem(slot) ?: run {
                inventory.setItem(slot, display.toItem())
                null
            }
        }
        set(value) {
            oldItem = item
            if (value != null) {
                inventory.setItem(slot, value)
                return
            }
            inventory.setItem(slot, display.toItem())
        }

    @Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
    inner class Interacted(private val event: InventoryInteractEvent, val action: SlotAction) {

        @OptIn(ExperimentalContracts::class)
        fun isDrag(event: InventoryInteractEvent): Boolean {
            contract {
                returns(true) implies (event is InventoryDragEvent)
                returns(false) implies (event is InventoryClickEvent)
            }
            return event is InventoryDragEvent
        }

        val oldItem get() = this@Slot.oldItem
        var item: ItemStack?
            get() = this@Slot.item
            set(value) {
                this@Slot.item = value
            }
        var cursor: ItemStack = if (isDrag(event)) (event.cursor ?: ItemStack(Material.AIR)) else event.cursor
            set(value) {
                if (isDrag(event)) event.cursor = value else event.setCursor(value)
            }
    }
}

enum class ItemDeleteReason { MENU_CLOSED, SLOT_WAS_HIDDEN }
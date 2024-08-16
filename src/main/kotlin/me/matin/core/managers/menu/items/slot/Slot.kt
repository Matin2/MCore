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
    defaultItem: ItemStack? = null,
    val show: Boolean = true,
    val allowTake: Boolean = true,
    val itemPredicate: (ItemStack) -> Boolean = { true },
    val itemDeleteAction: ((item: ItemStack, reason: ItemDeleteReason) -> Unit)? = null,
    val interactAction: Interacted.() -> Unit = {}
) {

    lateinit var inventory: Inventory
    private var _oldItem: ItemStack? = null
    val oldItem: ItemStack? get() = _oldItem
    var item: ItemStack? = defaultItem
        set(value) {
            _oldItem = field
            field = value?.takeUnless { it.isEmpty }
            value?.takeUnless { it.type == Material.AIR || it.amount == 0 } ?: display.toItem()
            inventory.setItem(slot, field ?: display.toItem())
        }

    @Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
    inner class Interacted(private val event: InventoryInteractEvent, val action: SlotAction) {

        @OptIn(ExperimentalContracts::class)
        private fun isDrag(event: InventoryInteractEvent): Boolean {
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
        var cursor: ItemStack? = if (isDrag(event)) getCursor(event.cursor) else getCursor(event.cursor)
            set(value) {
                val newValue = value ?: ItemStack(Material.AIR)
                if (isDrag(event)) event.cursor = newValue else event.setCursor(newValue)
                field = newValue
            }

        private fun getCursor(item: ItemStack?): ItemStack? = item?.takeUnless { it.isEmpty }
    }
}

enum class ItemDeleteReason { MENU_CLOSED, SLOT_WAS_HIDDEN }